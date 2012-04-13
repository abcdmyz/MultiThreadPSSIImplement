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
	
	public static void initial()
	{
		/*
		lockTable.clear();
		
		int i;
		
		for ( i=0; i<Parameter.hotspotSize; i++ )
		{
			int kSeq = HotSpot.getHotspotData(i);
			ReentrantLock lock = new ReentrantLock();		
			lockTable.put(kSeq, lock);
		}
		*/
	}	
	
	public static void checkLockExist( int kSeq )
	{
		if ( lockTable.get(kSeq) == null )
		{
			ReentrantLock lock = new ReentrantLock();		
			lockTable.put(kSeq, lock);
		}
	}

	public static ReentrantLock getLock( int kSeq )
	{
		checkLockExist(kSeq);
		return lockTable.get(kSeq);
	}
}
