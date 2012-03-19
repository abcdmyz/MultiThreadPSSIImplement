package module.server;

import java.util.HashSet;

import module.database.DataOperation;
import module.database.JDBCConnection;
import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;

public class RandomRows
{
	public static int[] randomSelectRow( int threadID )
	{
		long temp;
		int index = 0, i, j;
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
				
				index = (int) Math.abs( temp % Parameter.hotspotSize );
				
				//System.out.println("index " + index); 
			
				if ( !selectSet.contains(index) )
					diff = true;
			}
			
			selectRow[i] = HotSpot.getHotspotData(index);
			
			//System.out.println("Transaction " + threadID + " select " + index + " ");
			
			selectSet.add(index);
		}
		
		return selectRow;
	}
	
	public static int[] randomUpdateRow( int[] selectRow, int threadID )
	{
		long temp;
		int index = 0, i, j;
		int[] updateRow = new int[Parameter.updateSize];
		HashSet<Integer> selectSet = new HashSet<Integer>();
		HashSet<Integer> updateSet = new HashSet<Integer>();
		boolean diff;

		java.util.Random random =new java.util.Random();
		
		selectSet.clear();
		updateSet.clear();
		
		for ( i=0; i<Parameter.selectSize; i++ )
			selectSet.add(selectRow[i]);
		
		
		
		for ( i=0; i<Parameter.updateSize; i++ )
		{
			diff = false;
			
			while ( !diff )
			{
				temp = random.nextLong();
				
				index = (int) Math.abs( temp % Parameter.hotspotSize );
				
				//System.out.println("index " + index); 
			
				if ( !selectSet.contains(HotSpot.getHotspotData(index)) && !updateSet.contains(index) )
					diff = true;
			}
			
			
			
			updateRow[i] = HotSpot.getHotspotData(index);
			
			System.out.println("Transaction " + threadID + " update " + index + " ");
			
			updateSet.add(index);
		}
		
		
		return updateRow;
	}
}
