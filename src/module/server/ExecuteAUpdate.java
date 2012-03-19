package module.server;

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
	
	public void execute( NewProxyConnection connection, int transactionID, int kSeq, int fraction ) throws InterruptedException
	{
		if ( getLock(transactionID, kSeq) )
		{
			excuteUpdate(connection, kSeq, fraction);
			commitTransaction(connection, transactionID, kSeq);
		}
		else
		{
			abortTransaction(connection, transactionID, kSeq);
			FUWAbort++;
		}
			
	}
	
	public synchronized boolean getLock( int transactionID, int kSeq ) throws InterruptedException
	{
		if ( !SILockManager.getLock(kSeq).tryLock() )
		{
			System.out.println("Transaction " + transactionID + " Waiting " + kSeq);
			SILockManager.getLock(kSeq).lock();
		    
			if ( PSSITransactionManager.getTransactionState(PSSILockManager.getLastLocker(kSeq)) != null )
			{
				System.out.println("Transaction " + transactionID + " WaitFail " + kSeq);
				return false;
			}
			
			return false;
		}
		else
		{
			System.out.println("Transaction " + transactionID + " Lock " + kSeq);
			return true;
		}
	}
	
	public void excuteUpdate( NewProxyConnection connection, int kSeq, int fraction )
	{
		DataOperation.updataARow(connection, kSeq, fraction);
	}
	
	public synchronized void commitTransaction( NewProxyConnection connection, int transactionID, int kSeq )
	{
		TransactionOperation.commitTransaction(connection);
		
		PSSILockManager.setLastLocker(kSeq, transactionID);
		
		PSSITransactionManager.commitTransaction(transactionID);
		
		System.out.println("Transaction " + transactionID + " Commit");
		
		SILockManager.getLock(kSeq).unlock();
	}
	
	public synchronized void abortTransaction( NewProxyConnection connection, int transactionID, int kSeq )
	{
		TransactionOperation.abortTransaction(connection);
		
		PSSILockManager.setLastLocker(kSeq, transactionID);
	
		PSSITransactionManager.abortTransaction(transactionID);
		
		System.out.println("Transaction " + transactionID + " abort");
		
		SILockManager.getLock(kSeq).unlock();
	}
}
