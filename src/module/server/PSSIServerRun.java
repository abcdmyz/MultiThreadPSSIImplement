package module.server;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import module.PSSI.PSSIJudge;
import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransaction;
import module.PSSI.PSSITransactionManager;
import module.database.DataOperation;
import module.database.JDBCConnection;
import module.database.TransactionOperation;
import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;


public class PSSIServerRun implements Runnable
{
	long transactionID;
	long initialID;
	CountDownLatch cdl;
	
	public PSSIServerRun( long transactionID , CountDownLatch cdl )
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
		
		int average, i, j, k, fraction;
		
		
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
			PSSITransactionManager.startTransaction(transactionID);
			PSSIJudge.startTransaction(transactionID);
		
			selectRow = RandomRows.randomSelectRows(transactionID);
			updateRow = RandomRows.randomAUpdateRow(selectRow, transactionID);
			
			
			
			//updateRow = RandomRows.randomSelectRow(transactionID);
			//selectRow = RandomRows.randomUpdateRow(updateRow, transactionID);
				
			TransactionOperation.startTransaction(connection);
			
			average = DataOperation.selectData(connection, selectRow);
			fraction = (int) (average*0.001); 
			
			//for ( i=0; i<selectRow.length; i++ )
				//System.out.println("aa " + transactionID + " " + selectRow[i] + " " + PSSILockManager.checkLockExist(selectRow[i]));
			
			/**
			 * For PSSI
			 */
			//PSSILockManager.addSelectOperation(transactionID, selectRow);
			PSSITransactionManager.addSelectOperation(transactionID, selectRow);
			/**
			 * For PSSI
			 */
			
			
			ExecuteAPSSIUpdate executeAUpdate = new ExecuteAPSSIUpdate();
			try
			{
				executeAUpdate.execute(connection, transactionID, updateRow, fraction, selectRow );
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
