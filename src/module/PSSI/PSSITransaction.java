package module.PSSI;

import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


public class PSSITransaction
{
	long transactionID;
	String transactionState;
	long startTime, endTime;
	ReentrantReadWriteLock rwLock;
	
	private Vector<PSSIOperation> operationList = new Vector<PSSIOperation>();
	
	public PSSITransaction()
	{
		
	}
	
	public PSSITransaction( long transactionID, String transactionState )
	{
		this.transactionID = transactionID;
		this.transactionState = transactionState;
		operationList.clear();
		
		startTime = System.currentTimeMillis();
		rwLock = new ReentrantReadWriteLock();
	}
	
	public ReentrantReadWriteLock.ReadLock getReadLock()
	{
		return rwLock.readLock();
	}
	
	public ReentrantReadWriteLock.WriteLock getWriteLock()
	{
		return rwLock.writeLock();
	}
	
	public void addOperation( long transactionID, int kSeq, String RW)
	{
		PSSIOperation operation = new PSSIOperation(transactionID, kSeq, RW);
		operationList.add(operation);
	}
	
	public long getTransactionID()
	{
		return transactionID;
	}
	
	public String getTransactionState()
	{
		return transactionState;
	}
	
	public void setTransactionState( String state)
	{
		transactionState = state;
	}
	
	public Vector getOperationList()
	{
		return operationList;
	}
	
	public void printOperationList()
	{
		int i;
		
		for ( i=0; i<operationList.size(); i++ )
			System.out.println("TOL " + operationList.get(i).getTransactionID() + " " + operationList.get(i).getkSeq() + " " + operationList.get(i).getRW() + " " + operationList.get(i).getRW());
	}
	
	public void commitTransaction()
	{
		endTime = System.currentTimeMillis();
		transactionState = "commit";
	}
	
	public void abortTransaction()
	{
		endTime = System.currentTimeMillis();
		transactionState = "abort";
	}
	
	public long getStartTime()
	{
		return startTime;
	}
	
	public long getEndTime()
	{
		return endTime;
	}
}
