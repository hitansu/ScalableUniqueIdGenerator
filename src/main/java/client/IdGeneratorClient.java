package client;


import java.util.concurrent.CyclicBarrier;

import org.apache.ignite.Ignition;

import idgen_service.IdGenerationServiceDataLayer;
import persist_service.ConnectionManager;
import persist_service.IdPersistService;

/**
 * This class mimics the behaviour of 20 users concurrently trying to generate
 * unique id for a String.
 * The id for the String should be unique across the system
 * 
 * i,e for EPIC- ,the id generated should be EPIC-1,EPIC-2,EPIC-3,EPIC-4 ...
 *         STORY-,the id generated should be STORY-1,STORY-2,STORY-3,STORY-4 ...
 * 
 * @author hitansu
 *
 */
public class IdGeneratorClient {
	
  //static Set<String> set= new HashSet<String>();
    CyclicBarrier waiter; // to know when all finishes so that total time taken can be calculated
    int concurrency= 20; // no of users concurrently generating id
    static long startTime;
    
	public IdGeneratorClient() {
		
		waiter= new CyclicBarrier(concurrency, new Runnable() {
			
			public void run() {
				System.out.println("Printing start time ... "+startTime);
				System.out.println("Total time taken:: "+(System.currentTimeMillis()-startTime));
			//	System.out.println("Total "+set.size()+" id generated");
				ConnectionManager.releaseAllConnection();
				
			}
		});
	}
	
	public static void main(String[] args) {
		ConnectionManager.initialize();
		Ignition.setClientMode(true);
		
		IdGeneratorClient client= new IdGeneratorClient();
		startTime= System.currentTimeMillis();
		client.startIdGenTask();

	}
	
	private void startIdGenTask() {
		for(int i= 1;i<= concurrency;i++) {
			new Thread(new IdGenTask(waiter, new IdGenerationServiceDataLayer()), "User-"+i).start();
		}	
	}
}
