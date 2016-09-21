package ignite;


import java.util.concurrent.locks.Lock;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
/**
 * Ignite APIs related to AtomicSequence
 * 
 * @author hitansu
 *
 */
public class IdGenerationServiceCacheLayer {
	
	static Ignite ignite;
	static IgniteCache<String, Long> cache;
	static final IdGenerationServiceCacheLayer INSTANCE= new IdGenerationServiceCacheLayer();
	
	static {
		ignite= Ignition.start();
		cache= getCacheInstance(ignite);
	}
	
	private static IgniteCache<String, Long> getCacheInstance(Ignite ignite) {
		CacheConfiguration<String, Long> conf = new CacheConfiguration<String, Long>();
		conf.setName("id_cache");
		conf.setCacheMode(CacheMode.PARTITIONED);
		conf.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
		if (ignite.cache("id_cache") == null) {
			return ignite.createCache(conf);
		}
		return ignite.cache("id_cache");
	}
	
	public static IdGenerationServiceCacheLayer getInstance() {
		return INSTANCE;
	}
	
	
	public boolean isIdGeneratorAlive(String key, long defaultstart) {
		IgniteAtomicSequence seq = ignite.atomicSequence(key, defaultstart, false);
		if(seq== null)
			return false;
		else 
			return true;
	}
	
	public void startId(String key, long lastSaveId) {
		ignite.atomicSequence(key, lastSaveId, true);
	}
	
	public long getId(String key, long lastSaveId) {
		IgniteAtomicSequence seq = ignite.atomicSequence(key, lastSaveId, true);
		return seq.incrementAndGet();
	}
	
	public void increamentId(String key) {
		IgniteAtomicSequence seq = ignite.atomicSequence(key, 0, false);
		if(seq== null)
			throw new RuntimeException("cant be increamented");
		seq.incrementAndGet();
	}
	
	public Lock getLock(String key) {
		return cache.lock(key);
	}
	
	public long getCurrentValue(String key) {
		IgniteAtomicSequence seq = ignite.atomicSequence(key, 0, false);
		if(seq== null)
			return Integer.MIN_VALUE; // not present
		return seq.get();
	}
}
