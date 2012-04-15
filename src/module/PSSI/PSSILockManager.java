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
		if ( !lockTable.get(kSeq).getWriteLock().tryLock() )
		{
			System.out.println("Lock Manager Wait Add Update Transaction" + transactionID);
			lockTable.get(kSeq).getWriteLock().lock();
		}
		
		PSSILock lock = lockTable.get(kSeq);
		
		lock.addOperation(transactionID, kSeq, "w");
		
		lockTable.get(kSeq).getWriteLock().unlock();
	}
	
	
	public static void addSelectOperation( long transactionID, int[] selectRow )
	{
		int i;
		PSSILock lock = new PSSILock();
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			if ( !lockTable.get(selectRow[i]).getWriteLock().tryLock() )
			{
				System.out.println("Lock Manager Wait Add Select Transaction" + transactionID);
				lockTable.get(selectRow[i]).getWriteLock().lock();
			}
			
			lock = lockTable.get(selectRow[i]);
			
			//System.out.println(selectRow[i] + " " + lockTable.containsKey(selectRow[i]) + " " + lock.toString());
			
			lock.addOperation(transactionID, selectRow[i], "r");
			
			//lock.printOperationList();
			
			lockTable.get(selectRow[i]).getWriteLock().unlock();
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
		int kSeq;
		PSSIOperation operation = new PSSIOperation();
		
		for ( int i=0; i<transactionOperationList.size(); i++ )
		{
			kSeq = transactionOperationList.get(i).getkSeq();
			
			if ( !lockTable.get(kSeq).getWriteLock().tryLock() )
			{
				System.out.println("Lock Manager Wait Abort Transaction" + transactionID);
				lockTable.get(kSeq).getWriteLock().lock();
			}
			
			removeTransactionbyKseq( transactionID, transactionOperationList.get(i).getkSeq());
			 
	
			lockTable.get(kSeq).getWriteLock().unlock();
		}
	}
	
	public static void removeTransactionbyKseq( long transactionID, int Kseq)
	{	
		PSSILock lock = lockTable.get(Kseq);
		
		Vector<PSSIOperation> lockList = lock.getOperationList();
		java.util.Iterator<PSSIOperation> locklistIterator = lockList.iterator();
		PSSIOperation lockOperation;
		long tID;
		int index = -1;
	
		while ( locklistIterator.hasNext() )
		{
			lockOperation = (PSSIOperation) locklistIterator.next();
			
			tID = lockOperation.getTransactionID();
			
			if ( tID == transactionID )
			{
				index = lockList.indexOf(lockOperation);
				break;
			}
		}
		
		if ( index != -1 )
			lockList.remove(index);
		
		//System.out.println("remove " + transactionID + " " + Kseq + " " + lock.getOperationList().contains(tID));
	}
	
}
