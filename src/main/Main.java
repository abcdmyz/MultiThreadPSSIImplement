package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;



import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

import module.PSSI.PSSIJudge;
import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransactionManager;
import module.SI.SILockManager;
import module.TwoPL.TwoPLLockManager;
import module.TwoPL.TwoPLTransactionManager;
import module.client.ClientRequest;
import module.database.DatabaseStartUp;
import module.database.JDBCConnection;
import module.database.DataOperation;
import module.server.ExecuteAPSSIUpdate;
import module.server.ExecuteTwoPL;
import module.setting.Parameter;
import module.setting.Parameter;

public class Main
{
	public static void main( String[] args ) throws SQLException, InterruptedException, FileNotFoundException
	{
		PrintWriter printFile = new PrintWriter("e:\\a.txt");
		
		
		
		//for ( int i=0; i<20; i++ )
		
		{
			
			/*
			String OUTPUT = "C:/output.txt";
			 PrintStream outstream = null; 
			 outstream = new PrintStream(new FileOutputStream(OUTPUT));
			*/
			
			CountDownLatch cdl = new CountDownLatch(Parameter.threadSize);
			long startTime, endTime;
			double totalTime;
			
			startTime = System.currentTimeMillis();
			
			JDBCConnection.initial();
			
			//DatabaseStartUp.generateData();
			
			
			
			PSSITransactionManager.initial();
			PSSILockManager.initial();
			
			SILockManager.initial();
			PSSIJudge.initial();
			
			//TwoPLLockManager.initial();
			//TwoPLTransactionManager.initial();
			
			ExecuteTwoPL.initial();
			
			ClientRequest.send(cdl);
			
			cdl.await();
			
			endTime = System.currentTimeMillis();
			
			totalTime =  (double)(endTime-startTime)/1000;
			
			System.out.println("PSSI");
			//System.out.println("2PL");
			
		
			System.out.println("Total Hotspot Row: " + Parameter.hotspotSize);
			System.out.println("HostSpot Access Rate: " + Parameter.hotspotAccessRate);
			System.out.println("Total Thread: " + Parameter.threadSize);
			System.out.println("Transaction Per Thread: " + Parameter.transactionPerThread);
			System.out.println("Total Transaction: " + Parameter.transactionPerThread * Parameter.threadSize);
			System.out.println("Select " + Parameter.selectSize + " Update " + Parameter.updateSize);
			System.out.println("Total Time: " + totalTime);
			
			/*
			System.out.println("Total committed Transaction: " + ExecuteTwoPL.getCommittedTransactionCount());
			System.out.println("Transaction Peer Second: " + (int)((ExecuteTwoPL.getCommittedTransactionCount())/totalTime));
			System.out.println("rw Conflict: " + ExecuteTwoPL.getrwConflict());
			*/
			
			
			System.out.println("Total committed Transaction: " + ExecuteAPSSIUpdate.getCommittedTransactionCount());	
			System.out.println("Total FUW Abort: " + ExecuteAPSSIUpdate.getFUWAbort());
			System.out.println("Total PSSI Abort: " + ExecuteAPSSIUpdate.getPSSIAbort());
			System.out.println("Transaction Peer Second: " + (int)((ExecuteAPSSIUpdate.getCommittedTransactionCount())/totalTime));
			System.out.println("FUW Abort Peer Second: " + (int)(ExecuteAPSSIUpdate.getFUWAbort()/totalTime));
			System.out.println("PSSI Abort Peer Second: " + (int)(ExecuteAPSSIUpdate.getPSSIAbort()/totalTime));
			
			System.out.println("Node Per Thread: " + Parameter.nodeKeppPerThread);
			
		}

	}
}
