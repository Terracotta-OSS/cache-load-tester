package org.terracotta.ehcache.testing.statistics;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;

import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MemoryStatsCollector implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(MemoryStatsCollector.class);

  // Fields values to change
  private static final int reportPeriod = Integer.parseInt(System.getProperty("stats.reporter.interval","4"));

  private          long                       waitingInterval = TimeUnit.SECONDS.toMillis(reportPeriod * 2);

  public  volatile boolean                    running         = true;
  private          Map<Ehcache, CacheWrapper> cacheWrapperMap;

  public MemoryStatsCollector(final Map<Ehcache, CacheWrapper> cacheWrapperMap) {
    this.cacheWrapperMap = cacheWrapperMap;
  }

  public MemoryStatsCollector cacheWrappers(Map<Ehcache, CacheWrapper> cacheWrapperMap) {
    this.cacheWrapperMap = cacheWrapperMap;
    return this;
  }

  public void run() {
    try {
      while (running) {
        logger.info("----------- Memory -----------");
        for (Ehcache cache : cacheWrapperMap.keySet()) {
          if (Status.STATUS_ALIVE.equals(cache.getStatus())) {
            CacheWrapper cacheWrapper = cacheWrapperMap.get(cache);
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
