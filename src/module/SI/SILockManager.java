package module.SI;

import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import module.setting.Parameter;

public class SILockManager
{
	private static ConcurrentHashMap<Integer, ReentrantLock> lockTable = new ConcurrentHashMap<Integer, ReentrantLock>();
	private static ReentrantLock newLock = new ReentrantLock();
	
	public static void initial()
	{
		
		lockTable.clear();
		
		int i;
		
		for ( i=1; i<=Parameter.dataSetSize; i++ )
		{
			ReentrantLock lock = new ReentrantLock();		
			lockTable.put(i, lock);
		}
		
	}	
	
	public static boolean checkLockExist( int kSeq )
	{
		return lockTable.containsKey(kSeq);
	}

	public static ReentrantLock getLock( int kSeq )
	{
		if ( !lockTable.containsKey(kSeq) )
		{
		
			//System.out.println("New Lock " + kSeq);
			
			ReentrantLock lock = new ReentrantLock();		
			lockTable.put(kSeq, lock);
			
		}
		
		return lockTable.get(kSeq);
	}
	
	public static ReentrantLock getNewLock()
	{
		return newLock;
	}
}
