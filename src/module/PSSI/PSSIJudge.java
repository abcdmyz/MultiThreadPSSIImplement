package module.PSSI;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.text.html.HTMLDocument.Iterator;

import module.setting.Parameter;


public class PSSIJudge
{
	private static HashSet<Long> mark = new HashSet<Long>(); 
	private static ConcurrentHashMap<Long, LinkedList<Long>> DGT = new ConcurrentHashMap<Long, LinkedList<Long>>();
	private static ReentrantLock DGTLock = new ReentrantLock();
		
	private static ConcurrentHashMap<Long, LinkedList<Long>> DGTRV = new ConcurrentHashMap<Long, LinkedList<Long>>();
	
	/*
	 * Lock Node in DGT, Lock Transaction
	 */
	private static ConcurrentHashMap<Long, ReentrantLock> lockTable = new ConcurrentHashMap<Long, ReentrantLock>();
	
	
	public static void initial()
	{		
		DGT.clear();
		
		/**
		 * DGTV
		 */
		DGTRV.clear();
		
		

		lockTable.clear();
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
		
		ReentrantLock lock = new ReentrantLock();		
		lockTable.put(tID, lock);
		
		//System.out.println("new tID " + tID);
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
		LinkedList<Long> nodeList;
		Long node;
		java.util.Iterator<Long> iterator;
	
		Long longid = new Long(transactionID);
		
		nodeList = DGTRV.get(transactionID);
		if ( nodeList != null )
		{
			iterator = nodeList.iterator();
		
		//for ( int i=0; i<nodeList.size(); i++ )
			while ( iterator.hasNext() )
			{
				node = iterator.next();
				
				if ( DGT.get(node) != null )
				{
					//System.out.println("AAA " + node);
					DGT.get(node).remove(longid);
				}
			}
			
			DGT.remove(transactionID);
		}
		
		
		nodeList = DGTRV.get(transactionID);
		
		if ( nodeList != null )
		{
			iterator = nodeList.iterator();
			
			//for ( int i=0; i<nodeList.size(); i++ )
			while ( iterator.hasNext() )
			{
				node = iterator.next();
				
				if ( DGTRV.get(node) != null )
				{
					//System.out.println("AAA");
					DGTRV.get(node).remove(longid);
				}
			}
			
			DGTRV.remove(transactionID);
		}
		
		lockTable.remove(transactionID);
		
	}
	
	public static boolean commitTransaction( long tID, int updateRow, int[] selectRow ) throws InterruptedException, FileNotFoundException
	{
		boolean returnMessage = false;
		Vector<PSSIOperation> transactionList = PSSITransactionManager.getTransaction(tID).getOperationList();
		
		java.util.Iterator<PSSIOperation> translistIterator = transactionList.iterator();
		PSSIOperation transactionOperation;
		int kSeq;
		
		/**
		 * Build Graph
		 */
		while ( !buildGraph(tID, updateRow, selectRow) );
		
		mark.clear();
		returnMessage = judgeCircle(tID);
		//returnMessage = false;
		
		if ( returnMessage )
		{
			//System.out.println("abort transaction " + tID);
			removeTransaction(tID);
		}
		
		/*
		int IDgap = (int) ((tID % Parameter.transactionIDGap) - Parameter.nodeKeppPerThread);
		int removetID = (int) tID - Parameter.nodeKeppPerThread;
		//System.out.println(tID + " " + removetID);
		if ( IDgap > 0 && PSSITransactionManager.checkTransactionExist(removetID) )
		{
			if ( PSSITransactionManager.checkCommitTransaction(removetID) )
			{
				//System.out.println("remove old transaction " + removetID);
			
			
				removeTransaction(removetID);
	
				transactionList = PSSITransactionManager.getTransaction(removetID).getOperationList();
				translistIterator = transactionList.iterator();
			
				while ( translistIterator.hasNext() )
				{
					transactionOperation = (PSSIOperation) translistIterator.next();
					kSeq = transactionOperation.getkSeq();
					PSSILockManager.removeTransactionbyKseq(removetID, kSeq);
				}
				
				PSSITransactionManager.removeTransaction(removetID);
				
			}
		}
		*/
		
		return returnMessage;
	}
	
	
	public static boolean buildGraph( long tID, int updateRow, int[] selectRow ) throws InterruptedException, FileNotFoundException
	{
		Vector<PSSIOperation> transactionList = PSSITransactionManager.getTransaction(tID).getOperationList();
		Vector<PSSIOperation> lockList;
		java.util.Iterator<PSSIOperation> locklistIterator;
		int kSeq;
		long commitTID;
		
		PSSIOperation transactionOperation;
		PSSIOperation lockOperation;
		
		PrintWriter printFile = new PrintWriter("e:\\a.txt");
		
		/**
		 * Partition Graph
		 */
		LinkedList<Long> nodeLockList = new LinkedList<Long>();
		ListIterator<Long> nodeLockListIterator;
		long nodeID = 0;
		nodeLockList.clear();
		/**
		 * Partition Graph
		 */
		
		
		//PSSILockManager.addUpdateOperation(tID, updateRow);
		//PSSILockManager.addSelectOperation(tID, selectRow);
		
		ReentrantReadWriteLock.ReadLock transReadCTLock = null;
		ReentrantReadWriteLock.ReadLock transReadTLock = null;
		
		//mayAbortEdge.clear();
		
		//System.out.println("-----------------------------" + tID);
		
		/**
		 * Partition Graph
		 */
		if ( !nodeLockList.contains(tID) )
		{
			if ( !lockTable.get(tID).tryLock(1, TimeUnit.SECONDS) )
			{
				/*
				ListIterator<Long> nodeLockListIterator = nodeLockList.listIterator();
				
				while ( nodeLockListIterator.hasNext() )
				{
					nodeID = nodeLockListIterator.next();
					
					//System.out.println("nodeID " +  nodeID);
					
					lockTable.get(nodeID).unlock();
					
					System.out.println("1 Transaction " + tID + " Release " + nodeID + " Lock");
				}
				*/
				
				
				System.out.println("1 Transaction " + tID + " Wait " + tID + " Fail Restart");
				
				System.out.println("1 return");
				
				return false;
			}
	
			nodeLockList.add(tID);
			System.out.println("1 Transaction " + tID + " Get " + tID + " Lock");
		}
		/**
		 * Partition Graph
		 */
		
		
		
		transReadTLock = PSSITransactionManager.getReadLock(tID);
		
		
		if( !transReadTLock.tryLock() )
		{
			System.out.println("PSSI Wait Read Current Transaction " + tID);
			transReadTLock.lock();
		}
		
		//System.out.println("PSSI get Current Read Transaction" + tID);
		
	
		java.util.Iterator<PSSIOperation> translistIterator = transactionList.iterator();
		
		//for ( int i=0; i<transactionList.size(); i++ )
		
		
		while ( translistIterator.hasNext() )
		{
			transactionOperation = (PSSIOperation) translistIterator.next();
			
			//kSeq = transactionList.get(i).getkSeq();
			kSeq = transactionOperation.getkSeq();
			
			if ( !PSSILockManager.getLock(kSeq).getReadLock().tryLock() )
				PSSILockManager.getLock(kSeq).getReadLock().lock();
			
			lockList = PSSILockManager.getLock(kSeq).getOperationList();
			
			
			if ( lockList == null )
				continue;
			
			//System.out.println(lockList.size());
			
			locklistIterator = lockList.iterator();
			
			//for ( int j=0; j<lockList.size(); j++ )
			
			while ( locklistIterator.hasNext() )
			{
				lockOperation = (PSSIOperation) locklistIterator.next();
				
				//commitTID = lockList.get(j).getTransactionID();
				commitTID = lockOperation.getTransactionID();
				
				//System.out.print("ttID " + ttID);
				
				if ( commitTID == tID )
				{
					 continue;
				}
				
				//System.out.println("get " + commitTID + " " + kSeq + " tID " + tID);
				
				transReadCTLock = PSSITransactionManager.getReadLock(commitTID);
				
				if( !transReadCTLock.tryLock() )
				{
					System.out.println("PSSI Wait Read Commit Transaction " + commitTID);
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
				
				/**
				 * Partition Graph
				 */
				if ( !nodeLockList.contains(commitTID) ) 
				{
					if ( !lockTable.get(commitTID).tryLock(1, TimeUnit.SECONDS) )
					{
						nodeLockListIterator = nodeLockList.listIterator();
						
						while ( nodeLockListIterator.hasNext() )
						{
							nodeID = nodeLockListIterator.next();
							
							//System.out.println("nodeID " +  nodeID);
							
							System.out.println("2 Transaction " + tID + " Release " + nodeID + " Lock " + lockTable.get(nodeID).getHoldCount() );
							
							while ( lockTable.get(nodeID).getHoldCount() > 0 )
								lockTable.get(nodeID).unlock();
							
							System.out.println("2 Transaction " + tID + " Release " + nodeID + " Lock");
						}
						
						System.out.println("2 Transaction " + tID + " Wait " + commitTID + " Fail Restart");
						
						transReadCTLock.unlock();
						transReadTLock.unlock();
						PSSILockManager.getLock(kSeq).getReadLock().unlock();
						
						System.out.println("2 return");
						
						return false;
					}
				
				
					nodeLockList.add(commitTID);
					System.out.println("2 Transaction " + tID + " Get " + commitTID + " Lock");
				}
				/**
				 * Partition Graph
				 */
				
				//System.out.println(transactionList.get(i).getTransactionID() + " " + transactionList.get(i).getRW() + " " + lockList.get(j).getTransactionID()+ " " + lockList.get(j).getRW());
				
				
				
				if ( PSSITransactionManager.getTransactionEndTime(commitTID) >PSSITransactionManager.getTransactionStartTime(tID) )
				{
					//System.out.println(tID);
					//if (  transactionList.get(i).getRW().equals("r") && lockList.get(j).getRW().equals("w") )
					if ( transactionOperation.getRW().equals("r") && lockOperation.getRW().equals("w") )
					{
						addEdge(tID, commitTID);
					}
					//else if (  transactionList.get(i).getRW().equals("w") && lockList.get(j).getRW().equals("r") )
					else if (  transactionOperation.getRW().equals("w") && lockOperation.getRW().equals("r") )
					{
						addEdge(commitTID, tID);
					}
				}
				else
				{
					//if (  transactionList.get(i).getRW().equals("w") || lockList.get(j).getRW().equals("w") )
					if (  transactionOperation.getRW().equals("w") || lockOperation.getRW().equals("w") )
					{
						addEdge(commitTID,tID);
					}
				}
				
				//System.out.println("PSSI Release Commit Transaction" + commitTID);
				transReadCTLock.unlock();
			}
			
			PSSILockManager.getLock(kSeq).getReadLock().unlock();	
		}
		
		//System.out.println("PSSI Release Current Transaction" + tID);
		transReadTLock.unlock();
		
	
		
		
		nodeLockListIterator = nodeLockList.listIterator();
		
		while ( nodeLockListIterator.hasNext() )
		{
			nodeID = nodeLockListIterator.next();
			
			System.out.println("3 Transaction " + tID + " Release " + nodeID + " Lock " + lockTable.get(nodeID).getHoldCount());
			
			while ( lockTable.get(nodeID).getHoldCount() > 0 )
				lockTable.get(nodeID).unlock();
			
			
		}
		
		System.out.println("3 return");
		
		
		return true;
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
		
			if ( !PSSITransactionManager.checkCommitTransaction(temp) )
				continue;
			
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
