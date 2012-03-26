package main;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import Test.Test;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

import module.PSSI.PSSIJudge;
import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransactionManager;
import module.SI.SILockManager;
import module.client.ClientRequest;
import module.database.DatabaseStartUp;
import module.database.JDBCConnection;
import module.database.DataOperation;
import module.server.ExecuteAUpdate;
import module.server.HotSpot;
import module.setting.Parameter;
import module.setting.Parameter;

public class Main
{
	public static void main( String[] args ) throws SQLException, InterruptedException
	{
		CountDownLatch cdl = new CountDownLatch(Parameter.threadSize);
		long startTime, endTime;
		double totalTime;
		
		startTime = System.currentTimeMillis();
		
		JDBCConnection.initial();
		
		//DatabaseStartUp.generateData();
		HotSpot.generateHotspotData();
		
		PSSITransactionManager.initial();
		PSSILockManager.initial();
		
		SILockManager.initial();
		PSSIJudge.initial();
		
		ClientRequest.send(cdl);
		
		cdl.await();
		
		endTime = System.currentTimeMillis();
		
		totalTime =  (double)(endTime-startTime)/1000;
		
		System.out.println("Total Hotspot Row: " + Parameter.hotspotSize);
		System.out.println("Total Transaction: " + Parameter.transactionSize);
		System.out.println("Total Thread: " + Parameter.threadSize);
		System.out.println("Total FUW Abort: " + ExecuteAUpdate.getFUWAbort());
		System.out.println("Total PSSI Abort: " + ExecuteAUpdate.getPSSIAbort());
		
		System.out.println("Totla Time: " + totalTime);
		System.out.println("FUW Abort Peer Second: " + (int)(ExecuteAUpdate.getFUWAbort()/totalTime));
		System.out.println("PSSI Abort Peer Second: " + (int)(ExecuteAUpdate.getPSSIAbort()/totalTime));
		
	}
}
