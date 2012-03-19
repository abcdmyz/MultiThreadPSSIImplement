package module.PSSI;

import java.util.LinkedList;
import java.util.Vector;


public class PSSITransaction
{
	int transactionID;
	String transactionState;
	
	private Vector<Operation> operationList = new Vector<Operation>();
	
	public PSSITransaction()
	{
		
	}
	
	public PSSITransaction( int transactionID, String transactionState )
	{
		this.transactionID = transactionID;
		this.transactionState = transactionState;
		operationList.clear();
	}
	
	public void addOperation( int transactionID, int kSeq, String RW, int position )
	{
		Operation operation = new Operation(transactionID, kSeq, RW, position);
		operationList.add(operation);
	}
	
	public int getTransactionID()
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
}
