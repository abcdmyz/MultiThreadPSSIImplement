package module.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mchange.v2.c3p0.impl.NewProxyConnection;

import module.database.JDBCConnection;
import module.setting.Parameter;

public class HotSpot
{
	private static int[] hotspotData = new int[Parameter.hotspotSize+50];
	
	public static void generateHotspotData() 
	{
		int i;
		long temp;
		int kseq;
		
		java.util.Random random =new java.util.Random();
		
		for ( i=0; i<Parameter.hotspotSize; i++ )
		{
			temp = random.nextLong();
			kseq = (int) Math.abs( temp % Parameter.dataSetSize );
			
			//System.out.println(kseq);
			
			hotspotData[i] = kseq;
		}
	}
	
	public static int getHotspotData( int i )
	{
		return hotspotData[i];
	}
}

