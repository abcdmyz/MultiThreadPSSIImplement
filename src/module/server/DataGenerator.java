package module.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mchange.v2.c3p0.impl.NewProxyConnection;

import module.database.JDBCConnection;
import module.setting.Parameter;

public class DataGenerator
{
	private static int[] hotspotData = new int[Parameter.hotspotSize+50];
	
	public static int generateKseq() 
	{
		int i;
		long temp;
		int rate, kseq;
		
		java.util.Random random =new java.util.Random();
		
		
		temp = random.nextLong();
		rate = (int) Math.abs( temp % 100 );
			
		if ( rate < Parameter.hotspotAccessRate )
		{
			temp = random.nextLong();
			kseq = (int) Math.abs( temp % Parameter.hotspotSize );
		}
		else
		{
			temp = random.nextLong();
			kseq = (int) Math.abs( temp % ( Parameter.dataSetSize - Parameter.hotspotSize)) + Parameter.hotspotSize;
		}
		
		return kseq;	
	}
}

