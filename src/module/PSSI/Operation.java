package module.PSSI;

public class Operation
{
	private int transactionID;
	private int kSeq;
	private String RW;
	private int position;
	
	public Operation()
	{
		
	}
	
	public Operation( int transactionID, int kSeq, String RW )
	{
		this.transactionID = transactionID;
		this.kSeq = kSeq;
		this.RW = RW;
		this.position = 0;
	}
	
	public Operation( int transactionID, int kSeq, String RW, int position )
	{
		this.transactionID = transactionID;
		this.kSeq = kSeq;
		this.RW = RW;
		this.position = position;
	}
	
	public int getTransactionID()
	{
		return transactionID;
	}
	public void setTransactionID(int transactionID)
	{
		this.transactionID = transactionID;
	}
	public int getkSeq()
	{
		return kSeq;
	}
	public void setkSeq(int kSeq)
	{
		this.kSeq = kSeq;
	}
	public String getRW()
	{
		return RW;
	}
	public void setRW(String rW)
	{
		RW = rW;
	}
	public int getPosition()
	{
		return position;
	}
	public void setPosition(int position)
	{
		this.position = position;
	}
	
	
}
