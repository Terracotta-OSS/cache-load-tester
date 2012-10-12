package org.terracotta.ehcache.testing.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;


public class MemoryStatsCollector implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(MemoryStatsCollector.class);

  // Fields values to change
  private static final int reportPeriod = Integer.parseInt(System.getProperty("stats.reporter.interval","4"));

  private final long waitingInterval = TimeUnit.SECONDS.toMillis(reportPeriod * 2);

  public  volatile boolean running         = true;
  private  Set<CacheWrapper> cacheWrapperMap;

  public MemoryStatsCollector(final Set<CacheWrapper> cacheWrapperMap) {
    this.cacheWrapperMap = cacheWrapperMap;
  }

  public MemoryStatsCollector cacheWrappers(Set<CacheWrapper> cacheWrapperMap) {
    this.cacheWrapperMap = cacheWrapperMap;
    return this;
  }

  public void run() {
    try {
      while (running) {
        logger.info("----------- Memory -----------");

        // Get non-duplicate set of the ehcaches
        Map<String, CacheWrapper> caches = new HashMap<String, CacheWrapper>();
        for (CacheWrapper cacheWrapper : cacheWrapperMap)
            caches.put(cacheWrapper.getCache().getName(), cacheWrapper);

        for (CacheWrapper cacheWrapper: caches.values()) {
          if (Status.STATUS_ALIVE.equals(cacheWrapper.getCache().getStatus())) {
            logger.info("Cache name = {} \t\t OnHeap={}Kb \t\t OffHeap={}Kb \t\t OnDisk={}Kb", new Object[] {
                cacheWrapper.getName(),
                cacheWrapper.getOnHeapSize(),
                cacheWrapper.getOffHeapSize(),
                cacheWrapper.getOnDiskSize()
            });
          }
        }
        logger.info("------------------------------");

        Thread.sleep(waitingInterval);
      }
    } catch (InterruptedException ex) {
      running = false;
      Thread.currentThread().interrupt();
    }
  }

  public void stop() {
    running = false;
  }
}
