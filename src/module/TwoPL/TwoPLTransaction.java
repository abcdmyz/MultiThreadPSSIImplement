package module.TwoPL;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TwoPLTransaction
{
	long transactionID;
	String transactionState;
	long startTime, endTime;
	ReentrantReadWriteLock rwLock;
	
	public TwoPLTransaction(  )
	{
	
	}

	public TwoPLTransaction( long transactionID, String transactionState )
	{
		this.transactionID = transactionID;
		this.transactionState = transactionState;
		
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
