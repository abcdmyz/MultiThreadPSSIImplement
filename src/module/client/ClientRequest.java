package module.client;

import java.util.concurrent.CountDownLatch;

import module.server.ExecuteAPSSIUpdate;
import module.server.PSSIServerRun;
import module.server.TwoPLServerRun;
import module.setting.Parameter;

public class ClientRequest
{
	public static void send( CountDownLatch cdl ) 
	{
		int i;
		int transactionIDInitial = Parameter.transactionIDInitial;
		
		for ( i=0; i<Parameter.threadSize; i++ )
		{
			//PSSIServerRun sr = new PSSIServerRun( transactionIDInitial, cdl );
			
			TwoPLServerRun sr = new TwoPLServerRun( transactionIDInitial, cdl );
			
			Thread serverThread = new Thread(sr);
			serverThread.start();
			
			transactionIDInitial += Parameter.transactionIDGap;
		}

	}

}
 