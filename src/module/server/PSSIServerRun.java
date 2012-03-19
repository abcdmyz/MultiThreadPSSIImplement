package module.server;

import java.sql.SQLException;
import java.util.LinkedList;

import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransaction;
import module.PSSI.PSSITransactionManager;
import module.database.DataOperation;
import module.database.JDBCConnection;
import module.database.TransactionOperation;
import module.setting.Parameter;

import com.mchange.v2.c3p0.impl.NewProxyConnection;


public class PSSIServerRun implements Runnable
{
	int transactionID;
	
	public PSSIServerRun( int transactionID )
	{
		this.transactionID = transactionID;
	}
	
	public void run()
	{
		// TODO Auto-generated method stub
		
		//System.out.println("run");
		
		int[] selectRow = new int[Parameter.selectSize];
		int[] updateRow = new int[Parameter.updateSize];
		int[] selectPosition = new int[Parameter.selectSize];
		int average, i, j, k, fraction;
		PSSITransaction transaction = new PSSITransaction();
		
		for ( k=0; k<Parameter.transactionSize/Parameter.threadSize; k++ )
		{
			PSSITransactionManager.startTransaction(transactionID);
		
			selectRow = RandomRows.randomSelectRow(transactionID);
			updateRow = RandomRows.randomUpdateRow(selectRow, transactionID);
			
			
			NewProxyConnection connection = null;
			try
			{
				connection = (NewProxyConnection) JDBCConnection.getConnection();
			}
			catch ( SQLException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try
			{
				connection.setAutoCommit(false);
			}
			catch ( SQLException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			TransactionOperation.startTransaction(connection);
			
			average = DataOperation.selectData(connection, selectRow);
			fraction = (int) (average*0.001); 
			
			/**
			 * For PSSI
			 */
			selectPosition = PSSILockManager.addSelectOperation(transactionID, selectRow);
			PSSITransactionManager.addSelectOperation(transactionID, selectRow, selectPosition);
			
			
			ExecuteAUpdate executeAUpdate = new ExecuteAUpdate();
			try
			{
				executeAUpdate.execute(connection, transactionID, updateRow[0], fraction );
			}
			catch ( InterruptedException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
			
			transactionID++;
		}
	}
	
}
