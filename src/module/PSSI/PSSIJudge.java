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
	private static LinkedList<Edge> mayAbortEdge = new LinkedList();
	
	
	public static void initial()
	{		
		DGT.clear();
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
	}
	
	public static void addEdge( long tID, long ttID )
	{	
		if ( !DGT.get(tID).contains(ttID) )
		{
			System.out.println("add edge " + tID + " " + ttID);
			DGT.get(tID).add(ttID);
		}
	}
	
	public static void addMayAbortEdge( long ttID, long tID )
	{
		Edge edge = new Edge(ttID, tID);
		mayAbortEdge.add(edge);
	}
	
	public static void removeTransaction( long transactionID )
	{
		for ( int i=0; i<mayAbortEdge.size(); i++ )
		{
			if ( mayAbortEdge.get(i).getNodeB() == transactionID )
			{
				DGT.get(mayAbortEdge.get(i).getNodeA()).remove(mayAbortEdge.get(i).getNodeB());
			}
			System.out.println("PSSI Remove Edge 2 " + mayAbortEdge.get(i).getNodeA() + " " + mayAbortEdge.get(i).getNodeB());
		}
		
		DGT.remove(transactionID);
	}
	
	
	
	public static boolean commitTransaction( long tID )
	{
		Vector<PSSIOperation> transactionList = PSSITransactionManager.getTransaction(tID).getOperationList();
		Vector<PSSIOperation> lockList;
		int kSeq;
		long commitTID;
		boolean returnMessage = false;
		
		ReentrantReadWriteLock.WriteLock transReadCTLock = null;
		ReentrantReadWriteLock.WriteLock transReadTLock = null;
		
		mayAbortEdge.clear();
		
		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA   " + tID);
		
		transReadTLock = PSSITransactionManager.getWriteLock(tID);
		
		
		if( !transReadTLock.tryLock() )
		{
			System.out.println("PSSI Wait Current Read Transaction" + tID);
			transReadTLock.lock();
		}
		
		System.out.println("PSSI get Current Read Transaction" + tID);
		
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
				
				transReadCTLock = PSSITransactionManager.getWriteLock(commitTID);
				
				if( !transReadCTLock.tryLock() )
				{
					System.out.println("PSSI Wait Commit Read Transaction" + commitTID);
					transReadCTLock.lock();
				}
				
				System.out.println("PSSI get Commit Read Transaction" + commitTID);
				
				if ( PSSITransactionManager.checkAbortTransaction(commitTID) )
				{
					System.out.println(commitTID + " abort");
					System.out.println("PSSI Release Commit Transaction" + commitTID);
					transReadCTLock.unlock();
					continue;
				}
				
				if ( !PSSITransactionManager.checkCommitTransaction(commitTID) )
				{
					System.out.println(commitTID + " not commit");
					System.out.println("PSSI Release Commit Transaction" + commitTID);
					transReadCTLock.unlock();
					continue;
				}
				
				
				
				System.out.println(transactionList.get(i).getTransactionID() + " " + transactionList.get(i).getRW() + " " + lockList.get(j).getTransactionID()+ " " + lockList.get(j).getRW());
				
				//( transactionList.get(i).getRW().equals("w") || lockList.get(j).getRW().equals("w") )
				
				if ( PSSITransactionManager.getTransactionEndTime(commitTID) >PSSITransactionManager.getTransactionStartTime(tID) )
				{
					if (  transactionList.get(i).getRW().equals("r") && lockList.get(j).getRW().equals("w") )
					{
						addEdge(tID, commitTID);
					}
					else if (  transactionList.get(i).getRW().equals("w") && lockList.get(j).getRW().equals("r") )
					{
						addEdge(commitTID, tID);
						
						addMayAbortEdge(commitTID, tID);
					}
				}
				else
				{
					if (  transactionList.get(i).getRW().equals("w") || lockList.get(j).getRW().equals("w") )
					{
						addEdge(commitTID,tID);
						
						addMayAbortEdge(commitTID, tID);
					}
				}
				
				System.out.println("PSSI Release Commit Transaction" + commitTID);
				transReadCTLock.unlock();
			}
				
		}
		
		System.out.println("PSSI Release Current Transaction" + tID);
		transReadTLock.unlock();
		
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
		
		System.out.println("tid size " + tID + " " + DGT.get(tID).size());
		
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
