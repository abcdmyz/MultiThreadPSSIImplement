package module.server;

import java.util.HashSet;

import module.database.DataOperation;
import module.database.JDBCConnection;
import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;

public class RandomRows
{
	public static int[] randomSelectRows( long transactionID  )
	{
		long temp;
		int kseq = 0, i, j;
		int[] selectRow = new int[Parameter.selectSize];
		boolean diff;
		
		HashSet<Integer> selectSet = new HashSet<Integer>();
		
		
		java.util.Random random =new java.util.Random();
		
		selectSet.clear();
		
		
		for ( i=0; i<Parameter.selectSize; i++ )
		{
			diff = false;
			
			while ( !diff )
			{
				temp = random.nextLong();
				
				kseq = DataGenerator.generateKseq();
				
				//System.out.println("index " + index); 
			
				if ( !selectSet.contains(kseq) )
					diff = true;
			}
			
			selectRow[i] = kseq;
			
			//System.out.println("Transaction " + transactionID + " select " + index + " " + HotSpot.getHotspotData(index));
			
			selectSet.add(kseq);
		}
		
		return selectRow;
	}
	
	public static int randomAUpdateRow( int[] selectRow, long transactionID )
	{
		long temp;
		int kseq = 0, i, j;
		int updateRow = 0;
		HashSet<Integer> selectSet = new HashSet<Integer>();

		boolean diff;

		java.util.Random random =new java.util.Random();
		
		selectSet.clear();
		
		for ( i=0; i<Parameter.selectSize; i++ )
			selectSet.add(selectRow[i]);
		
		diff = false;
			
		while ( !diff )
		{
			temp = random.nextLong();
				
			kseq = DataGenerator.generateKseq();
				
				//System.out.println("index " + index); 
			
			if ( !selectSet.contains(kseq)  )
				diff = true;
		}
	
		updateRow = kseq;
			
			//System.out.println("Transaction " + transactionID + " update " + index + " " + HotSpot.getHotspotData(index));
		
		return updateRow;
	}
}
