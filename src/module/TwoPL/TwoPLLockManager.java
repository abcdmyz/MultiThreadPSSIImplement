package module.TwoPL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import module.PSSI.PSSILock;
import module.setting.Parameter;


public class TwoPLLockManager
{
	private static ConcurrentHashMap<Integer, ReentrantReadWriteLock> lockTable = new ConcurrentHashMap<Integer, ReentrantReadWriteLock>();
	
	public static void initial()
	{
		/*
		lockTable.clear();
		
		int i;
		int kSeq = 0;
		
		for ( i=0; i<Parameter.hotspotSize; i++ )
		{
			kSeq = HotSpot.getHotspotData(i);
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock();		
			lockTable.put(kSeq, lock);
			
			//System.out.println("kSeq " + kSeq + " " + lockTable.containsKey(kSeq));
		}
		
		//System.out.println("kSeq " + kSeq + " " + lockTable.containsKey(kSeq));
		 * 
		 */
	}
	
	public static void checkLockExist( int kSeq )
	{
		if ( lockTable.get(kSeq) == null )
		{
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock();		
			lockTable.put(kSeq, lock);
		}
	}

	public static ReentrantReadWriteLock.ReadLock getReadLock( int kSeq )
	{
		//System.out.println(kSeq + " " + lockTable.containsKey(kSeq));
		checkLockExist(kSeq);
		
		return lockTable.get(kSeq).readLock();
	}
	
	public static ReentrantReadWriteLock.WriteLock getWriteLock( int kSeq )
	{
		checkLockExist(kSeq);
		return lockTable.get(kSeq).writeLock();
	}
	
	public static ReentrantReadWriteLock getLock( int kSeq )
	{
		checkLockExist(kSeq);
		return lockTable.get(kSeq);
	}
}
