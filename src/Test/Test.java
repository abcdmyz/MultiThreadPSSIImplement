package Test;

import java.sql.SQLException;

import module.database.JDBCConnection;
import module.database.DataOperation;
import module.server.HotSpot;
import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;

public class Test
{
	public static void rwData() throws SQLException
	{
		long temp;
		int index, average;
		int[] selectRead = new int[Parameter.selectSize];
		int[] selectUpdate= new int[Parameter.updateSize];
		
		NewProxyConnection connection = (NewProxyConnection) JDBCConnection.getConnection();
		
		java.util.Random random =new java.util.Random();
		
		for ( int i=0; i<Parameter.selectSize; i++ )
		{
			temp = random.nextLong();
			index = (int) Math.abs( temp % Parameter.hotspotSize );
			
			selectRead[i] = HotSpot.getHotspotData(index);
		}
		
		average = DataOperation.selectData(connection, selectRead);
		System.out.println("average " + average);
		
		
		for ( int i=0; i<Parameter.updateSize; i++ )
		{
			temp = random.nextLong();
			index = (int) Math.abs( temp % Parameter.hotspotSize );
			
			selectUpdate[i] = HotSpot.getHotspotData(index);
		}
		
		DataOperation.updataData(connection, selectUpdate, average*0.001);
		
	}
}
