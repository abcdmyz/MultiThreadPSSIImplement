package module.PSSI;

import java.util.LinkedList;
import java.util.Vector;

public class PSSILock
{
	private int kSeq;
	private Vector<Operation> operationList = new Vector<Operation>();
	private int currentLocker;
	private int lastLocker;
	
	
	public PSSILock()
	{
		operationList.clear();
		setCurrentLocker(-1);
		lastLocker = -1;
	}	
	
	public PSSILock( int kSeq )
	{
		this.kSeq = kSeq;
		operationList.clear();
		setCurrentLocker(-1);
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
	
	
	
	public int addOperation( int transactionID, int kSeq, String RW )
	{
		Operation operation = new Operation(transactionID, kSeq, RW);
		operationList.add(operation);
		
		return operationList.size()-1;
	}
	
	
	public void printOperationList()
	{
		int i;
		
		for ( i=0; i<operationList.size(); i++ )
			System.out.println("LOL " + operationList.get(i).getTransactionID() + " " + operationList.get(i).getkSeq() + " " + operationList.get(i).getRW() + " " + operationList.get(i).getRW());
	}

	public int getLastLocker()
	{
		return lastLocker;
	}

	public void setLastLocker(int lastLocker)
	{
		this.lastLocker = lastLocker;
	}

	public int getCurrentLocker()
	{
		return currentLocker;
	}

	public void setCurrentLocker(int currentLocker)
	{
		this.currentLocker = currentLocker;
	}
}
