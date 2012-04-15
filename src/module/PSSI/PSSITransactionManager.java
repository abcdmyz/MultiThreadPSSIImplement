package module.PSSI;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import module.setting.Parameter;


public class PSSITransactionManager
{
	private static ConcurrentHashMap<Long, PSSITransaction> transactionTable = new ConcurrentHashMap<Long, PSSITransaction>();
	
	public static void initial()
	{
		transactionTable.clear();
	}
	
	public static void startTransaction( long transactionID )
	{
		PSSITransaction transaction = new PSSITransaction( transactionID, "active" );
		transactionTable.put(transactionID, transaction);
	}
	
	public static void abortTransaction( long transactionID )
	{
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		transaction.abortTransaction();
	}
	
	public static void commitTransaction(long transactionID)
	{
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		transaction.commitTransaction();
	}
	
	public static void addUpdateOperation( long transactionID, int kSeq )
	{
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		transaction.addOperation(transactionID, kSeq, "w");
	}
	
	public static void removeTransaction( long transactionID )
	{
		transactionTable.remove(transactionID);
	}
	
	public static PSSITransaction getTransaction( long transactionID )
	{
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		return transaction;
	}
	
	public static boolean checkTransactionExist( long transactionID )
	{
		if ( transactionTable.get(transactionID) == null )
			return false;
		return true;
				
	}
	
	public static boolean checkCommitTransaction( long transactionID )
	{
		if ( transactionTable.get(transactionID).getTransactionState().equals("commit") )
			return true;
		return false;
	}
	
	public static boolean checkAbortTransaction( long transactionID )
	{
		if ( transactionTable.get(transactionID).getTransactionState().equals("abort") )
			return true;
		return false;
	}
	
	public static void addSelectOperation( long transactionID, int[] selectRow )
	{
		int i;
		
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			transaction.addOperation(transactionID, selectRow[i], "r");
		}
		
		//transaction.printOperationList();
	}
	
	public static String getTransactionState( long transactionID )
	{
		if ( transactionTable.get(transactionID) != null )
			return transactionTable.get(transactionID).getTransactionState();
		
		return null;
	}
	
	public static void setTransactionState( int transactionID, String transactionState )
	{
		if ( transactionTable.get(transactionID) != null )
			transactionTable.get(transactionID).setTransactionState(transactionState);
	}
	
	public static long getTransactionStartTime( long tID )
	{
		return transactionTable.get(tID).getStartTime();
	}
	
	public static long getTransactionEndTime( long commitTID )
	{
		return transactionTable.get(commitTID).getEndTime();
	}
	
	public static ReentrantReadWriteLock.ReadLock getReadLock( long transactionID )
	{
		return transactionTable.get(transactionID).getReadLock();
	}
	
	public static ReentrantReadWriteLock.WriteLock getWriteLock( long transactionID )
	{
		return transactionTable.get(transactionID).getWriteLock();
	}
}
