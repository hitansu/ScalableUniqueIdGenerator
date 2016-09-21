package idgen_service;


import java.util.concurrent.locks.Lock;

import org.apache.ignite.IgniteException;

import ignite.IdGenerationServiceCacheLayer;
import persist_service.IdPersistService;

/**
 * This class is like an interface between user & ignite api to generate id.
 * After each 1000 ids generated for each String,it update the last updated id in the Database so that
 * in case of graceful system down it can fetch from DB & can start generating id next to the last id.
 * 
 * @author hitansu
 *
 */
public class IdGenerationServiceDataLayer {

	private IdGenerationServiceCacheLayer idgenCaheService;// contains ignite API code to generate id
	private IdPersistService persistService;
	private static final long DEFAULT_ID= 0; //default start id
	private static final int WRITE_INTERVAL= 1000; // after how ids we want to update to DB
	
	public IdGenerationServiceDataLayer() {
		idgenCaheService= IdGenerationServiceCacheLayer.getInstance();
		persistService= new IdPersistService();
	}
	
	public long generateId(String key, long defaultstart) {
		long start_id = 0;
		if(!idgenCaheService.isIdGeneratorAlive(key, defaultstart)) {
			long last_save_id = persistService.getIdFromDB(key);
			start_id= last_save_id== Integer.MIN_VALUE ? DEFAULT_ID:last_save_id;
			idgenCaheService.startId(key, start_id);
		}
		long currentId= idgenCaheService.getCurrentValue(key);
		if (currentId % WRITE_INTERVAL == 0) {
			// save the id to db .Before that take lock for that particular key
			//Take lock before writing to DB
			  Lock lock = idgenCaheService.getLock(key);
			  try {
				  lock.lock();
				  if(idgenCaheService.getCurrentValue(key) % WRITE_INTERVAL== 0) {
					  persistService.persistId(key, idgenCaheService.getCurrentValue(key)+WRITE_INTERVAL);
					  idgenCaheService.increamentId(key);
				  } 
			  } finally {
				  lock.unlock();
			  }
		}
		long next = 0;
		//TODO: This try/catch is added to handle the node failure.
		//Surprisingly shutting down one node Ignite AtomicSequnce throwing exception.
       // long next = idgenCaheService.getId(key, start_id); // this shud ideally suffice rather than retry logic
		
		/*retry logic*/
		try {
			next = idgenCaheService.getId(key, start_id);
		} catch(IgniteException e) {
			boolean maybeNodeDown= true;
			int retryCount= 1;
			while(retryCount<=5 && maybeNodeDown) {
				try {
					retryCount++;
				    next = idgenCaheService.getId(key, start_id);
					maybeNodeDown= false;
				} catch(IgniteException e1) {
					maybeNodeDown= true;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e2) {
						//
					}
				}
				
			}
		}
	
		return next;
	}
}
