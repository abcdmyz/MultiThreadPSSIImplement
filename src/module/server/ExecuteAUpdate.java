package module.server;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import module.PSSI.PSSIJudge;
import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransactionManager;
import module.SI.SILockManager;
import module.database.DataOperation;
import module.database.TransactionOperation;


import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

public class ExecuteAUpdate
{
	private static int FUWAbort = 0;
	private static int PSSIAbort = 0;
	
	private static ReentrantLock commitLock = new ReentrantLock();
	
	public void execute( NewProxyConnection connection, long transactionID, int kSeq, int fraction ) throws InterruptedException
	{
		if ( getLock(transactionID, kSeq) )
		{
			excuteUpdate(connection, kSeq, fraction);
			
			PSSILockManager.addUpdateOperation(transactionID, kSeq);
			PSSITransactionManager.addUpdateOperation(transactionID, kSeq);
			
			if ( !PSSIDetect(transactionID) )
			{
				System.out.println("**********PSSI NO Cycle " +  transactionID + " Commit");
				
				if ( !commitLock.tryLock() )
				{
					commitLock.tryLock();
				}
				
				commitTransaction(connection, transactionID, kSeq);
				
				
			}
			else
			{
				System.out.println("**********PSSI Has Cycle " +  transactionID + " Abort");
				abortTransaction(connection, transactionID, kSeq);
				addPSSIAbort();
			}
			
			PSSIJudge.getDGTLock().unlock();
		}
		else
		{
			System.out.println("------------------First Updater Win " +  transactionID + " Abort");
			
			abortTransaction(connection, transactionID, kSeq);
			addFUWAbort();
		}
		
		
		SILockManager.getLock(kSeq).unlock();	
	}
	
	public boolean PSSIDetect( long transactionID )
	{
		if ( !PSSIJudge.getDGTLock().tryLock() )
		{
			PSSIJudge.getDGTLock().lock();
		}
			
		return PSSIJudge.commitTransaction(transactionID);
	}

	
	public boolean getLock( long transactionID, int kSeq ) throws InterruptedException
	{
		ReentrantReadWriteLock.ReadLock transReadLock = null;
		
		if ( !SILockManager.getLock(kSeq).tryLock() )
		{
			System.out.println("========Transaction " + transactionID  + " wait");
			SILockManager.getLock(kSeq).lock();
		    
			System.out.println("========Transaction " + transactionID + " last locker " + PSSILockManager.getLastLocker(kSeq) + " " + PSSITransactionManager.getTransactionState(PSSILockManager.getLastLocker(kSeq))  );
			
			transReadLock = PSSITransactionManager.getReadLock(PSSILockManager.getLastLocker(kSeq));
			
			if( !transReadLock.tryLock() )
			{
				System.out.println("GetLock Wait to Read T_State" + PSSILockManager.getLastLocker(kSeq));
				transReadLock.lock();
			}
			
			System.out.println("GetLock get to Read T_State" + PSSILockManager.getLastLocker(kSeq));
			
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
			System.out.println("=========Transactin " + transactionID + " get lock");
			return true;
		}
	}
	
	public void excuteUpdate( NewProxyConnection connection, int kSeq, int fraction )
	{
		DataOperation.updataARow(connection, kSeq, fraction);
	}
	
	public void commitTransaction( NewProxyConnection connection, long transactionID, int kSeq )
	{
		ReentrantReadWriteLock.WriteLock transWriteLock = null;
		
		transWriteLock = PSSITransactionManager.getWriteLock(transactionID);
		
		if( !transWriteLock.tryLock() )
		{
			System.out.println("commit Transaction Wait Write Lock " + transactionID);
			transWriteLock.lock();
		}
		System.out.println("Commit Transaction get Write Lock " + transactionID);
		
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
		
		//SILockManager.getLock(kSeq).unlock();
	}
	
	public  void abortTransaction( NewProxyConnection connection, long transactionID, int kSeq )
	{
		ReentrantReadWriteLock.WriteLock transWriteLock = null;
		
		transWriteLock = PSSITransactionManager.getWriteLock(transactionID);
		
		if( !transWriteLock.tryLock() )
		{
			System.out.println("Abort Transaction Wait Write Lock " + transactionID);
			transWriteLock.lock();
		}
		
		System.out.println("Abort Transaction Get Write Lock " + transactionID);
		
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
	}
	
	public static int getPSSIAbort()
	{
		return PSSIAbort;
	}
}
