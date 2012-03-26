package module.PSSI;

public class PSSIOperation
{
	private long transactionID;
	private int kSeq;
	private String RW;
	
	public PSSIOperation()
	{
		
	}
	
	public PSSIOperation( long transactionID, int kSeq, String RW )
	{
		this.transactionID = transactionID;
		this.kSeq = kSeq;
		this.RW = RW;
	}
	
	public long getTransactionID()
	{
		return transactionID;
	}
	public void setTransactionID(long transactionID)
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

	
	
}
