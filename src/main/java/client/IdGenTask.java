package client;


import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import idgen_service.IdGenerationServiceDataLayer;

/**
 * This class represents a user
 * A user is generating 200000 ids in this example
 * 
 * @author hitansu
 *
 */
public class IdGenTask implements Runnable {

	final String[] prefixes = { "BUG-", "STORY-", "EPIC-", "SPRINT-", "TASK-", "ISSUE-", "US-", "TICKET-" };
	static final int count = 200000;
	
	private CyclicBarrier barrier;
	private Random rand;
	private IdGenerationServiceDataLayer idgenService;// just a layer of service to give id to user

	public IdGenTask(CyclicBarrier barrier, IdGenerationServiceDataLayer idgenService) {
		this.barrier = barrier;
		this.idgenService = idgenService;
		rand = new Random();
	}
	
	public void run() {
		try {
			generateSequnceId(count);
			barrier.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void generateSequnceId(int count) {
		for(int i = 1;  i <= count; i++) {
			String prefix_key = prefixes[rand.nextInt(prefixes.length)].trim();
			long id = idgenService.generateId(prefix_key, 0);
			System.out.println("key: "+prefix_key+" id: "+id);
		}
	}
}
