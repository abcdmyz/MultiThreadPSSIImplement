package module.server;

import java.io.FileNotFoundException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import main.Main;
import module.PSSI.PSSIJudge;
import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransactionManager;
import module.SI.SILockManager;
import module.database.DataOperation;
import module.database.TransactionOperation;


import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

public class ExecuteAPSSIUpdate
{
	private static int FUWAbort = 0;
	private static int PSSIAbort = 0;
	private static int committedTransaction = 0;
	
	private static ReentrantLock commitLock = new ReentrantLock();
	
	public void execute( Connection connection, long transactionID, int kSeq, int fraction, int[] selectRow) throws InterruptedException, FileNotFoundException
	{
		
		if ( getLock(transactionID, kSeq) )
		{
			excuteUpdate(connection, kSeq, fraction);
			
			//commitTransaction(connection, transactionID, kSeq);
			
			PSSILockManager.addUpdateOperation(transactionID, kSeq);
			PSSILockManager.addSelectOperation(transactionID, selectRow);
			
			PSSITransactionManager.addUpdateOperation(transactionID, kSeq);
			PSSITransactionManager.addSelectOperation(transactionID, selectRow);
		
			
			
			if ( !PSSIDetect(transactionID, kSeq, selectRow) )
			{
				//Main.logger.warn("**********PSSI NO Cycle " +  transactionID + " Commit");
				commitTransaction(connection, transactionID, kSeq);
				
				
			}
			else
			{
				//Main.logger.warn("**********PSSI Has Cycle " +  transactionID + " Abort");
				abortTransaction(connection, transactionID, kSeq);
				addPSSIAbort();
			}
			
			//PSSIJudge.getDGTLock().unlock();
			
		}
		else
		{
			//System.out.println("------------------First Updater Win " +  transactionID + " Abort");
			
			abortTransaction(connection, transactionID, kSeq);
			addFUWAbort();
		}
		
		
		SILockManager.getLock(kSeq).unlock();
		//System.out.println(SILockManager.getLock(kSeq).toString());	
	}
	
	public boolean PSSIDetect( long transactionID, int kSeq, int[] selectRow ) throws InterruptedException, FileNotFoundException
	{
		/**
		 * PSSI Judge Lock
		 */
		
		//if ( !PSSIJudge.getDGTLock().tryLock() )
		{
			//PSSIJudge.getDGTLock().lock();
		}
		
		
		return PSSIJudge.commitTransaction(transactionID, kSeq, selectRow);
	}

	
	public boolean getLock( long transactionID, int kSeq ) throws InterruptedException
	{
		//Read LastLocker
		ReentrantReadWriteLock.ReadLock transReadLock = null;

		/*
		if ( !SILockManager.checkLockExist(kSeq) )
		{
			if ( !SILockManager.getNewLock().tryLock() )
			{
				SILockManager.getNewLock().lock();
			}
		}
		*/
		
		/**
		 * !Update kSeq
		 */
		if ( !SILockManager.getLock(kSeq).tryLock() )
		{
			//System.out.println("========Transaction " + transactionID  + " wait");
			SILockManager.getLock(kSeq).lock();
		    
			//System.out.println("========Transaction " + transactionID + " last locker " + PSSILockManager.getLastLocker(kSeq) + " " + PSSITransactionManager.getTransactionState(PSSILockManager.getLastLocker(kSeq))  );
			
			if ( PSSILockManager.getLastLocker(kSeq) == -1 )
			{
				return true;
			}
			
			/**
			 * Read LastLocker
			 */
			transReadLock = PSSITransactionManager.getReadLock(PSSILockManager.getLastLocker(kSeq));
			
		
			if( !transReadLock.tryLock() )
			{
				//System.out.println("GetLock Wait to Read T_State " + PSSILockManager.getLastLocker(kSeq));
				transReadLock.lock();
			}
			
			//System.out.println("GetLock get to Read T_State " + PSSILockManager.getLastLocker(kSeq));
			
			
			
			if ( PSSITransactionManager.getTransactionState(PSSILockManager.getLastLocker(kSeq)).equals("commit") )
			{
				transReadLock.unlock();
				return false;
			}
			
			transReadLock.unlock();
			return true;
		}
		else
		{
			//System.out.println("=========Transactin " + transactionID + " get lock");
			return true;
		}
		
		
	}
	
	public void excuteUpdate( Connection connection, int kSeq, int fraction )
	{
		DataOperation.updataARow(connection, kSeq, fraction);
	}
	
	public void commitTransaction( Connection connection, long transactionID, int kSeq )
	{
		
		ReentrantReadWriteLock.WriteLock transWriteLock = null;
		
		
		/**
		 * Commit Transaction Lock
		 */
		transWriteLock = PSSITransactionManager.getWriteLock(transactionID);
		
		if( !transWriteLock.tryLock() )
		{
			//System.out.println("commit Transaction Wait Write Lock " + transactionID);
			transWriteLock.lock();
		}
		//System.out.println("Commit Transaction get Write Lock " + transactionID);
		
		TransactionOperation.commitTransaction(connection);
		
		/**
		 * For PSSI
		 */
		PSSILockManager.setLastLocker(kSeq, transactionID);
		PSSITransactionManager.commitTransaction(transactionID);
		/**
		 * For PSSI
		 */
		
		transWriteLock.unlock();
		
		addCommittedTransactionCount();
		
		//SILockManager.getLock(kSeq).unlock();
	}
	
	public  void abortTransaction( Connection connection, long transactionID, int kSeq )
	{
		ReentrantReadWriteLock.WriteLock transWriteLock = null;
		
		/**
		 * Abort Transaction Lock
		 */
		transWriteLock = PSSITransactionManager.getWriteLock(transactionID);
		
		if( !transWriteLock.tryLock() )
		{
			//System.out.println("Abort Transaction Wait Write Lock " + transactionID);
			transWriteLock.lock();
		}
		
		//System.out.println("Abort Transaction Get Write Lock " + transactionID);
		
		TransactionOperation.abortTransaction(connection);
		
		/**
		 * For PSSI
		 */
		PSSILockManager.setLastLocker(kSeq, transactionID);
		
		PSSITransactionManager.abortTransaction(transactionID);
		PSSILockManager.abortTransaction(transactionID);
		
		/**
		 * For PSSI
		 */
		
		transWriteLock.unlock();
		
		//SILockManager.getLock(kSeq).unlock();
	}

	public synchronized void addFUWAbort()
	{
		FUWAbort++;
	}
	
	public static int getFUWAbort()
	{
		return FUWAbort;
	}
	
	public synchronized void addPSSIAbort()
	{
		PSSIAbort++;
		//System.out.println(" " + PSSIAbort);
	}
	
	public static int getPSSIAbort()
	{
		return PSSIAbort;
	}
	
	public synchronized void addCommittedTransactionCount()
	{
		committedTransaction++;
	}
	
	public static int getCommittedTransactionCount()
	{
		return committedTransaction;
	}
}
