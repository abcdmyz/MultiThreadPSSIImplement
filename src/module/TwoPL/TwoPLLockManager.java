package module.TwoPL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import module.PSSI.PSSILock;
import module.setting.Parameter;


public class TwoPLLockManager
{
	private static ConcurrentHashMap<Integer, ReentrantReadWriteLock> lockTable = new ConcurrentHashMap<Integer, ReentrantReadWriteLock>();
	private static ReentrantLock newLock = new ReentrantLock();
	
	public static void initial()
	{
		
		lockTable.clear();
		
		int i;
		int kSeq = 0;
		
		for ( i=1; i<=Parameter.dataSetSize; i++ )
		{
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock();		
			lockTable.put(i, lock);
			
			//System.out.println("kSeq " + kSeq + " " + lockTable.containsKey(kSeq));
		}
		
		//System.out.println("kSeq " + kSeq + " " + lockTable.containsKey(kSeq));
		 
		
	}
	
	public static boolean checkLockExist( int kSeq )
	{
		return lockTable.containsKey(kSeq);
	}
	
	public static ReentrantReadWriteLock getLock( int kSeq )
	{
		if ( ! lockTable.containsKey(kSeq) )
		{
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock();		
			lockTable.put(kSeq, lock);
		}
		return lockTable.get(kSeq);
	}
	
	public static ReentrantLock getNewLock()
	{
		return newLock;
	}
}
