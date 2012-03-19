package module.PSSI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import module.server.HotSpot;
import module.setting.Parameter;



public class PSSILockManager
{
	private static ConcurrentHashMap<Integer, PSSILock> lockTable = new ConcurrentHashMap<Integer, PSSILock>();
	
	public static void initial()
	{
		lockTable.clear();
		
		int i;
		
		for ( i=0; i<Parameter.hotspotSize; i++ )
		{
			int kSeq = HotSpot.getHotspotData(i);
			PSSILock lock = new PSSILock(kSeq);		
			lockTable.put(kSeq, lock);
		}
	}
	
	public static int addOperation( int transactionID, int kSeq, String rw )
	{
		PSSILock lock = lockTable.get(kSeq);
		
		int count = lock.addOperation(transactionID, kSeq, rw);
		
		return count;
	}
	
	public static void addLock( int kSeq )
	{
		
	}
	

	public static void removeLock( int transactionID )
	{
		PSSITransaction transaction = PSSITransactionManager.getTransaction(transactionID);
		PSSILock lock = new PSSILock();
		Vector<Operation> transactionOperationList = transaction.getOperationList();
		Vector<Operation> lockOperationList = transaction.getOperationList();
		int kSeq;
		Operation operation = new Operation();
		
		for ( int i=0; i<transactionOperationList.size(); i++ )
		{
			kSeq = transactionOperationList.get(i).getkSeq();
			
			lock = lockTable.get(kSeq);
			lockOperationList = lock.getOperationList();
			
			ListIterator<Operation> iter = (ListIterator<Operation>) lockOperationList.iterator();
			 
			while ( iter.hasNext() )
			{
				operation = (Operation) iter.next();
				
				if ( operation.getTransactionID() == transactionID )
					iter.remove();	
			}
			
		}
	}
	
	public static PSSILock getLock( int kSeq )
	{
		return lockTable.get(kSeq);
	}
	
	public static int[] addSelectOperation( int transactionID, int[] selectRow )
	{
		int[] position = new int[Parameter.selectSize];
		int i;
		PSSILock lock = new PSSILock();
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			lock = lockTable.get(selectRow[i]);
			
			position[i] = lock.addOperation(transactionID, selectRow[i], "R");
			
			//lock.printOperationList();
			
		}
		
		return position;
	}
	
	public static int getCurrentLocker( int kSeq )
	{
		return lockTable.get(kSeq).getCurrentLocker();
	}
	
	public static void setCurrentLocker( int kSeq, int transactionID )
	{
		lockTable.get(kSeq).setCurrentLocker(transactionID);
	}
	
	public static int getLastLocker( int kSeq )
	{
		return lockTable.get(kSeq).getLastLocker();
	}
	
	public static void setLastLocker( int kSeq, int transactionID )
	{
		lockTable.get(kSeq).setLastLocker(transactionID);
	}
	
}
