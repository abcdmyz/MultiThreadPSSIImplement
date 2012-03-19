package module.PSSI;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import module.setting.Parameter;


public class PSSITransactionManager
{
	private static ConcurrentHashMap<Integer, PSSITransaction> transactionTable = new ConcurrentHashMap<Integer, PSSITransaction>();
	private static ConcurrentHashMap<Integer,Integer> commitTransactionSet= new ConcurrentHashMap<Integer,Integer>();
	
	public static void initial()
	{
		transactionTable.clear();
		commitTransactionSet.clear();
	}
	
	public static void startTransaction( int transactionID )
	{
		PSSITransaction transaction = new PSSITransaction( transactionID, "active" );
		transactionTable.put(transactionID, transaction);
	}
	
	public static void abortTransaction( int transactionID )
	{
		transactionTable.remove(transactionID);
	}
	
	public static void commitTransaction(int transactionID)
	{
		PSSITransaction transaction = transactionTable.get(transactionID);
		transaction.setTransactionState("commit");
		commitTransactionSet.put(transactionID,transactionID);
	}
	
	public static void addOperation( int transactionID, int kSeq, String RW, int position )
	{
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		transaction.addOperation(transactionID, kSeq, RW, position);
	}
	
	public static PSSITransaction getTransaction( int transactionID )
	{
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		return transaction;
	}
	
	public static boolean checkCommitTransaction( int transactionID )
	{
		return commitTransactionSet.contains(transactionID);
	}
	
	public static void addSelectOperation( int transactionID, int[] selectRow, int[] position )
	{
		int i;
		
		PSSITransaction transaction = transactionTable.get(transactionID);
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			transaction.addOperation(transactionID, selectRow[i], "R", position[i]);
		}
		
		//transaction.printOperationList();
	}
	
	public static String getTransactionState( int transactionID )
	{
		if ( transactionTable.get(transactionID) != null )
			return transactionTable.get(transactionID).getTransactionState();
		
		return null;
	}
	
	public static void setTransactionState( int transactionID )
	{
		if ( transactionTable.get(transactionID) != null )
			transactionTable.get(transactionID).setTransactionState("commit");
	}
}
