package main;

import java.sql.SQLException;

import Test.Test;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.mysql.jdbc.Connection;

import module.PSSI.PSSILockManager;
import module.PSSI.PSSITransactionManager;
import module.SI.SILockManager;
import module.client.ClientRequest;
import module.database.DatabaseStartUp;
import module.database.JDBCConnection;
import module.database.DataOperation;
import module.server.HotSpot;
import module.setting.Parameter;
import module.setting.Parameter;

public class Main
{
	public static void main( String[] args ) throws SQLException
	{
		JDBCConnection.initial();
		
		//DatabaseStartUp.generateData();
		HotSpot.generateHotspotData();
		
		PSSITransactionManager.initial();
		PSSILockManager.initial();
		
		SILockManager.initial();
		
		
		ClientRequest.send();
	}
}
