package module.PSSI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.html.HTMLDocument.Iterator;


import module.setting.Parameter;



public class PSSILockManager
{
	private static ConcurrentHashMap<Integer, PSSILock> lockTable = new ConcurrentHashMap<Integer, PSSILock>();
	
	public static void initial()
	{
		lockTable.clear();
		
		int i;
		
		for ( i=1; i<=Parameter.dataSetSize; i++ )
		{
			PSSILock lock = new PSSILock(i);		
			lockTable.put(i, lock);
		}
	}
	
	public static boolean checkLockExist( int kseq )
	{
		return lockTable.containsKey(kseq);
	}
	
	public static PSSILock getLock( int kseq )
	{
		if ( !lockTable.containsKey(kseq) )
		{
			PSSILock lock = new PSSILock(kseq);		
			lockTable.put(kseq, lock);
		}
		
		return lockTable.get(kseq);
	}
	
	public static void  addUpdateOperation( long transactionID, int kSeq )
	{
		PSSILock lock = lockTable.get(kSeq);
		
		lock.addOperation(transactionID, kSeq, "w");
	}
	
	
	public static void addSelectOperation( long transactionID, int[] selectRow )
	{
		int i;
		PSSILock lock = new PSSILock();
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			lock = lockTable.get(selectRow[i]);
			
			//System.out.println(selectRow[i] + " " + lockTable.containsKey(selectRow[i]) + " " + lock.toString());
			
			lock.addOperation(transactionID, selectRow[i], "r");
			
			//lock.printOperationList();
			
		}
	}
	
	
	public static long getLastLocker( int kSeq )
	{
		return lockTable.get(kSeq).getLastLocker();
	}
	
	public static void setLastLocker( int kSeq, long transactionID )
	{
		lockTable.get(kSeq).setLastLocker(transactionID);
	}
	
	public static void abortTransaction( long transactionID )
	{
		PSSITransaction transaction = PSSITransactionManager.getTransaction(transactionID);
		PSSILock lock = new PSSILock();
		Vector<PSSIOperation> transactionOperationList = transaction.getOperationList();
		Vector<PSSIOperation> lockOperationList = transaction.getOperationList();
		int kSeq;
		PSSIOperation operation = new PSSIOperation();
		
		for ( int i=0; i<transactionOperationList.size(); i++ )
		{
			kSeq = transactionOperationList.get(i).getkSeq();
			
			lock = lockTable.get(kSeq);
			lockOperationList = lock.getOperationList();
			
			java.util.Iterator<PSSIOperation> iter = lockOperationList.iterator();
			 
			/*
			while ( iter.hasNext() )
			{
				operation = iter.next();
				
				if ( operation.getTransactionID() == transactionID )
				{
					System.out.println("~~~~~remove " + operation.getTransactionID() + " " + operation.getkSeq() );
					iter.remove();
				}
			}
			*/
			
			for ( int j=0; j<lockOperationList.size(); j++ )
			{
				if ( lockOperationList.get(j).getTransactionID() == transactionID )
				{
					//System.out.println("~~~~~remove " +  lockOperationList.get(j).getTransactionID() + " " +  lockOperationList.get(j).getkSeq() );
					lockOperationList.remove(j);
				}
			}
			
		}
	}
	
}
