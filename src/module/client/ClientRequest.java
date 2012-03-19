package module.client;

import module.server.PSSIServerRun;
import module.setting.Parameter;

public class ClientRequest
{
	public static void send()
	{
		int i;
		int transactionIDInitial = 1000;
		
		for ( i=0; i<Parameter.threadSize; i++ )
		{
			PSSIServerRun sr = new PSSIServerRun( transactionIDInitial );
			Thread serverThread = new Thread(sr);
			serverThread.start();
			
			transactionIDInitial += 1000;
		}
	}

}
