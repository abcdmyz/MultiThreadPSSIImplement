package module.database;

import java.sql.SQLException;

import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

public class DatabaseStartUp
{
	public static void generateData() throws SQLException
	{
		long startTime, endTime;
		
		startTime = System.currentTimeMillis();
		
		deleteTable();
		//System.out.println("Delete Table");
		
		NewProxyConnection connection = (NewProxyConnection) JDBCConnection.getConnection();
		connection.setAutoCommit(false);
		
		String insertSt = "INSERT INTO bench VALUES (?, ?)";
		
		java.sql.PreparedStatement prest =  connection.prepareStatement(insertSt);
		
		java.util.Random random =new java.util.Random();
		long temp;
		int kval;
		
		for ( int i=1; i<= Parameter.dataSetSize; i++ )
		{ 
			temp = random.nextLong();
			kval = (int) (Math.abs( temp % Parameter.randomDataGap ) + Parameter.randomDataStart);
			
			prest.setInt(1, i);
			prest.setInt(2, kval);
			
			prest.addBatch();
			
			//System.out.println("Insert " + i);
		}
		
		//System.out.println("Execute Batch");
		prest.executeBatch();
		connection.commit();
		connection.close();
		
		endTime = System.currentTimeMillis();
		
		System.out.println("Generate Data Successfully");
		System.out.println("Total Time " + (int)(endTime-startTime)/1000);
	}
	
	public static void deleteTable() throws SQLException
	{
		NewProxyConnection connection = (NewProxyConnection) JDBCConnection.getConnection();
		
		String deleteString;
		deleteString = "DELETE FROM bench;";
		
		java.sql.Statement statement;
		
		statement = connection.createStatement();
		statement.executeUpdate(deleteString);
		statement.close();
		connection.close();
	}
}
