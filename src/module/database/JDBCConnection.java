package module.database;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class JDBCConnection
{
	static private String jdbc_driver = "com.mysql.jdbc.Driver";
	
	static private String mysql_url = "jdbc:mysql://127.0.0.1:3306/sicycles";
	static private String mysql_user = "root";
	static private String mysql_password = "root";
	
	static ComboPooledDataSource cpds;
	
	public static void initial() throws SQLException
	{
		buildPooledDataSource();
	}
	
	public static void buildPooledDataSource()
	{
		cpds = new ComboPooledDataSource();
		
		try
		{
			cpds.setDriverClass( jdbc_driver );
		}
		catch ( PropertyVetoException e1 )
		{
			e1.printStackTrace();
		} 
		
		cpds.setJdbcUrl( mysql_url );
		cpds.setUser(mysql_user);                                  
		cpds.setPassword(mysql_password);     
		
		cpds.setMaxStatements( 100 );	
	}
	
	public static void destroyPooledDataSource() throws SQLException
	{
		DataSources.destroy(cpds);
	}
	

	
	public static Connection getConnection() throws SQLException
	{
		Connection connection;

		connection = cpds.getConnection();
		
		return connection;
	}
	
	public static void closeConnection( Connection connection ) throws SQLException 
	{
		connection.close();
	}
}
