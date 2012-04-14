package module.server;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import module.TwoPL.TwoPLTransaction;
import module.TwoPL.TwoPLTransactionManager;
import module.database.DataOperation;
import module.database.JDBCConnection;
import module.database.TransactionOperation;
import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

public class TwoPLServerRun implements Runnable
{
	long transactionID;
	long initialID;
	CountDownLatch cdl;
	
	public TwoPLServerRun( long transactionID , CountDownLatch cdl )
	{
		this.transactionID = transactionID;
		this.initialID = transactionID;
		this.cdl = cdl;
	}
	
	public void run()
	{
		// TODO Auto-generated method stub
		
		//System.out.println("run");
		
		
		int[] selectRow = new int[Parameter.selectSize];
		int updateRow;
		int[] selectPosition = new int[Parameter.selectSize];
		int average, i, j, k, fraction;
		
		TwoPLTransaction transaction = new TwoPLTransaction();
		
		//NewProxyConnection connection = null;
		Connection connection = null;
		try
		{
			//connection = (NewProxyConnection) JDBCConnection.getConnection();
			connection = (Connection) JDBCConnection.getCommonConnection();
			connection.setAutoCommit(false);
			
		}
		catch ( SQLException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for ( k=0; k<Parameter.transactionPerThread; k++ )
		{
			
			TwoPLTransactionManager.startTransaction(transactionID);
		
			selectRow = RandomRows.randomSelectRows(transactionID);
			updateRow = RandomRows.randomAUpdateRow(selectRow, transactionID);
			
			
			
			TransactionOperation.startTransaction(connection);
			
			
			ExecuteTwoPL executeTPL = new ExecuteTwoPL();
			
			try
			{
				
				while ( !executeTPL.execute(connection, transactionID, selectRow, updateRow ) );
				{
					//TransactionOperation.abortTransaction(connection);
				}
			}
			catch ( InterruptedException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			
			
			
			transactionID++;
			
			if ( transactionID >= initialID + Parameter.transactionIDGap )
				transactionID = initialID;
			
			
		}
		
		
		try
		{
			connection.close();
		}
		catch ( SQLException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cdl.countDown();
	}
	
	
}
