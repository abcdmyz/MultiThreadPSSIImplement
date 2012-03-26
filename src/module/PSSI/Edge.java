package module.PSSI;

public class Edge
{
	private long nodeA;
	private long nodeB;
	
	public Edge( long ttID, long tID )
	{
		nodeA = ttID;
		nodeB = tID;
	}
	
	public long getNodeA()
	{
		return nodeA;
	}
	public void setNodeA(long nodeA)
	{
		this.nodeA = nodeA;
	}
	
	public long getNodeB()
	{
		return nodeB;
	}
	public void setNodeB(long nodeB)
	{
		this.nodeB = nodeB;
	}
}
