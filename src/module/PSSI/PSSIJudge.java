package module.PSSI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import module.setting.Parameter;


public class PSSIJudge
{
	private static HashSet<Long> mark = new HashSet<Long>(); 
	private static ConcurrentHashMap<Long, LinkedList<Long>> DGT = new ConcurrentHashMap<Long, LinkedList<Long>>();
	private static ReentrantLock DGTLock = new ReentrantLock();
		
	private static ConcurrentHashMap<Long, LinkedList<Long>> DGTRV = new ConcurrentHashMap<Long, LinkedList<Long>>();
	
	
	public static void initial()
	{		
		DGT.clear();
		
		/**
		 * DGTV
		 */
		DGTRV.clear();
	}
	
	
	public static ReentrantLock getDGTLock()
	{
		return DGTLock;
	}
	
	public static void startTransaction( long tID )
	{
		LinkedList<Long> nodeList = new LinkedList<Long>();
		nodeList.clear();
		DGT.put(tID, nodeList);
		
		/**
		 * DGTV
		 */
		LinkedList<Long> nodeList2 = new LinkedList<Long>();
		nodeList2.clear();
		DGTRV.put(tID, nodeList2);
	}
	
	public static void addEdge( long tID, long ttID )
	{	
		if ( !DGT.get(tID).contains(ttID) )
		{
			//System.out.println("add edge " + tID + " " + ttID);
			DGT.get(tID).add(ttID);
		}
		
		/**
		 * DGTV
		 */
		if ( !DGTRV.get(ttID).contains(tID) )
		{
			//System.out.println("add edge " + tID + " " + ttID);
			DGTRV.get(ttID).add(tID);
		}
	}

	
	public static void removeTransaction( long transactionID )
	{
		LinkedList<Long> nodeList = DGTRV.get(transactionID);
		
		Long longid = new Long(transactionID);
		
		for ( int i=0; i<nodeList.size(); i++ )
		{
			if ( DGT.get(nodeList.get(i)) == null )
				System.out.println("AAA");
			DGT.get(nodeList.get(i)).remove(longid);
		}
		
		nodeList = DGT.get(transactionID);
		
		for ( int i=0; i<nodeList.size(); i++ )
		{
			if ( DGTRV.get(nodeList.get(i)) == null )
				System.out.println("AAA");
			DGTRV.get(nodeList.get(i)).remove(longid);
		}
		
		DGT.remove(transactionID);
		DGTRV.remove(transactionID);
	}
	
	
	
	public static boolean commitTransaction( long tID, int updateRow, int[] selectRow )
	{
		Vector<PSSIOperation> transactionList = PSSITransactionManager.getTransaction(tID).getOperationList();
		Vector<PSSIOperation> lockList;
		int kSeq;
		long commitTID;
		boolean returnMessage = false;
		
		PSSILockManager.addUpdateOperation(tID, updateRow);
		PSSILockManager.addSelectOperation(tID, selectRow);
		
		ReentrantReadWriteLock.ReadLock transReadCTLock = null;
		ReentrantReadWriteLock.WriteLock transWriteTLock = null;
		
		//mayAbortEdge.clear();
		
		//System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA   " + tID);
		
		transWriteTLock = PSSITransactionManager.getWriteLock(tID);
		
		
		if( !transWriteTLock.tryLock() )
		{
			//System.out.println("PSSI Wait Current Read Transaction" + tID);
			transWriteTLock.lock();
		}
		
		//System.out.println("PSSI get Current Read Transaction" + tID);
		
		for ( int i=0; i<transactionList.size(); i++ )
		{
			kSeq = transactionList.get(i).getkSeq();
			
			lockList = PSSILockManager.getLock(kSeq).getOperationList();
			
			/**
			System.out.print(kSeq + " kSeq lockList ");
			for ( int dj=0; dj<lockList.size(); dj++ )
				System.out.print(lockList.get(dj).getTransactionID() + " ");
			System.out.println();
			**/
			
			if ( lockList == null )
				continue;
			
			for ( int j=0; j<lockList.size(); j++ )
			{
				commitTID = lockList.get(j).getTransactionID();
				
				//System.out.print("ttID " + ttID);
				
				if ( commitTID == tID )
				{
					continue;
				}
				
				transReadCTLock = PSSITransactionManager.getReadLock(commitTID);
				
				if( !transReadCTLock.tryLock() )
				{
					//System.out.println("PSSI Wait Commit Read Transaction" + commitTID);
					transReadCTLock.lock();
				}
				
				//System.out.println("PSSI get Commit Read Transaction" + commitTID);
				
				if ( PSSITransactionManager.checkAbortTransaction(commitTID) )
				{
					//System.out.println(commitTID + " abort");
					//System.out.println("PSSI Release Commit Transaction" + commitTID);
					transReadCTLock.unlock();
					continue;
				}
				
				if ( !PSSITransactionManager.checkCommitTransaction(commitTID) )
				{
					//System.out.println(commitTID + " not commit");
					//System.out.println("PSSI Release Commit Transaction" + commitTID);
					transReadCTLock.unlock();
					continue;
				}
				
				
				
				//System.out.println(transactionList.get(i).getTransactionID() + " " + transactionList.get(i).getRW() + " " + lockList.get(j).getTransactionID()+ " " + lockList.get(j).getRW());
				
				
				
				if ( PSSITransactionManager.getTransactionEndTime(commitTID) >PSSITransactionManager.getTransactionStartTime(tID) )
				{
					//System.out.println(tID);
					if (  transactionList.get(i).getRW().equals("r") && lockList.get(j).getRW().equals("w") )
					{
						addEdge(tID, commitTID);
					}
					else if (  transactionList.get(i).getRW().equals("w") && lockList.get(j).getRW().equals("r") )
					{
						addEdge(commitTID, tID);
					}
				}
				else
				{
					if (  transactionList.get(i).getRW().equals("w") || lockList.get(j).getRW().equals("w") )
					{
						addEdge(commitTID,tID);
					}
				}
				
				//System.out.println("PSSI Release Commit Transaction" + commitTID);
				transReadCTLock.unlock();
			}
				
		}
		
		//System.out.println("PSSI Release Current Transaction" + tID);
		transWriteTLock.unlock();
		
		mark.clear();
		returnMessage = judgeCircle(tID);
		
		if ( returnMessage )
			removeTransaction(tID);
		
		return returnMessage;
	}
	
	public static boolean judgeCircle( long tID )
	{
		boolean returnMessage = false;
		
		mark.add(tID);
		long temp;
		
		//System.out.println("tid size " + tID + " " + DGT.get(tID).size());
		
		for ( int i=0; i<DGT.get(tID).size(); i++ )
		{
			temp = DGT.get(tID).get(i);
					
			if ( mark.contains(temp) )
				return true;
			
			else
				returnMessage = judgeCircle(temp);
		}
			
		if ( returnMessage )
			return true;
		
		return false;
	}
	
	public static void abortTransaction( long tID )
	{
		DGT.remove(tID);
	}
}
