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
		int[] updateRow = new int[Parameter.updateSize];
		int[] selectPosition = new int[Parameter.selectSize];
		int average, i, j, k, fraction;
		PSSITransaction transaction = new PSSITransaction();
		
		for ( k=0; k<Parameter.transactionSize/Parameter.threadSize; k++ )
		{
			PSSITransactionManager.startTransaction(transactionID);
			PSSIJudge.startTransaction(transactionID);
		
			selectRow = RandomRows.randomSelectRow(transactionID);
			updateRow = RandomRows.randomUpdateRow(selectRow, transactionID);
			
			//updateRow = RandomRows.randomSelectRow(transactionID);
			//selectRow = RandomRows.randomUpdateRow(updateRow, transactionID);
			
			
			
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
			PSSILockManager.addSelectOperation(transactionID, selectRow);
			PSSITransactionManager.addSelectOperation(transactionID, selectRow);
			/**
			 * For PSSI
			 */
			
			
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
			
			if ( transactionID >= initialID + Parameter.transactionIDGap )
				transactionID = initialID;
			
		}
		
		cdl.countDown();
	}
	
}
