package module.server;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransactionManager;
import module.TwoPL.TwoPLLockManager;
import module.database.DataOperation;
import module.database.TransactionOperation;
import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

public class ExecuteTwoPL
{
	private static int committedTransaction;
	private static int rwConflict = 0;;
	
	public boolean execute( Connection connection, long transactionID, int[] selectRow, int updateRow) throws InterruptedException
	{
		int i, j, kval, sum, average, fraction;
		
		ReentrantReadWriteLock rowReadLock[] = new ReentrantReadWriteLock[Parameter.selectSize];
		ReentrantReadWriteLock rowWriteLock = null;
		
		sum = 0;
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			
			rowReadLock[i] = TwoPLLockManager.getLock(selectRow[i]);
			
			
			if ( !rowReadLock[i].readLock().tryLock(1, TimeUnit.SECONDS) )
			{
				//System.out.println("Restart Transaction " + transactionID + " Select Row " + selectRow[i]);
			
				for ( j=0; j<i; j++ )
					rowReadLock[j].readLock().unlock();
				
				addrwConflict();
					
				return false;
			}
			
			
			//System.out.println("========Transaction " + transactionID  + " Select Record " + selectRow[i]);
			kval = DataOperation.selectARow(connection, selectRow[i]);
			sum += kval;
			
		}
		
		
		
		average = sum / Parameter.selectSize;
		fraction = (int) (average*0.001); 
		
		
		rowWriteLock = TwoPLLockManager.getLock(updateRow);
		
		
		if ( !rowWriteLock.writeLock().tryLock(1, TimeUnit.SECONDS) )
		{
			//System.out.println("Restart Transaction " + transactionID + " Update Row " + updateRow);
			
			for ( i=0; i<Parameter.selectSize; i++ )
			{
				rowReadLock[i].readLock().unlock();
			}
			
			addrwConflict();
			
			return false;
		}
		
		
		//System.out.println("*********Transaction " + transactionID  + " Update Record " + updateRow);
		DataOperation.updataARow(connection, updateRow, fraction);
		
		
		TransactionOperation.commitTransaction(connection);
		addCommittedTransactionCount();
		
		rowWriteLock.writeLock().unlock();
		
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			rowReadLock[i].readLock().unlock();
		}

		return true;
	}
	
	public static void addCommittedTransactionCount()
	{
		committedTransaction++;
	}
	
	public static int getCommittedTransactionCount()
	{
		return committedTransaction;
	}
	
	public static void addrwConflict()
	{
		rwConflict++;
	}
	
	public static int getrwConflict()
	{
		return rwConflict;
	}
	
	public static void initial()
	{
		rwConflict = 0;
		committedTransaction = 0;
	}
}
