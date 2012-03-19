package module.SI;

import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import module.server.HotSpot;
import module.setting.Parameter;

public class SILockManager
{
	private static ConcurrentHashMap<Integer, ReentrantLock> lockTable = new ConcurrentHashMap<Integer, ReentrantLock>();
	
	public static void initial()
	{
		lockTable.clear();
		
		int i;
		
		for ( i=0; i<Parameter.hotspotSize; i++ )
		{
			int kSeq = HotSpot.getHotspotData(i);
			ReentrantLock lock = new ReentrantLock();		
			lockTable.put(kSeq, lock);
		}
	}	

	public static ReentrantLock getLock( int kSeq )
	{
		return lockTable.get(kSeq);
	}
}
