package module.PSSI;

import java.util.LinkedList;
import java.util.Vector;

public class PSSILock
{
	private int kSeq;
	private Vector<PSSIOperation> operationList = new Vector<PSSIOperation>();
	private long lastLocker;
	
	
	public PSSILock()
	{
		operationList.clear();
		lastLocker = -1;
	}	
	
	public PSSILock( int kSeq )
	{
		this.kSeq = kSeq;
		operationList.clear();
		lastLocker = -1;
	}

	public int getKSeq()
	{
		return kSeq;
	}
	
	
	public Vector getOperationList()
	{
		return operationList;
	}
	
	
	
	public void  addOperation( long transactionID, int kSeq, String RW )
	{	
		PSSIOperation operation = new PSSIOperation(transactionID, kSeq, RW);
		operationList.add(operation);
		
		//System.out.println("lock " + transactionID + " " + kSeq + " "  + operationList.size());
	}
	
	
	public void printOperationList()
	{
		int i;
		
		for ( i=0; i<operationList.size(); i++ )
			System.out.println("LOL " + operationList.get(i).getTransactionID() + " " + operationList.get(i).getkSeq() + " " + operationList.get(i).getRW() + " " + operationList.get(i).getRW());
	}

	public long getLastLocker()
	{
		return lastLocker;
	}

	public void setLastLocker(long transactionID)
	{
		this.lastLocker = transactionID;
	}


}
