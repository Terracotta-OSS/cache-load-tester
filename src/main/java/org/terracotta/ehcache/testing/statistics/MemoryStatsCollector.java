package org.terracotta.ehcache.testing.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;


public class MemoryStatsCollector implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(MemoryStatsCollector.class);

  // Fields values to change
  private static final int reportPeriod = Integer.parseInt(System.getProperty("stats.reporter.interval","4"));

  private final long waitingInterval = TimeUnit.SECONDS.toMillis(reportPeriod * 2);

  public  volatile boolean running         = true;
  private Collection<GenericCacheWrapper> cacheWrapperMap;

  public MemoryStatsCollector(final Collection<GenericCacheWrapper> cacheWrapperMap) {
    this.cacheWrapperMap = cacheWrapperMap;
  }

  public MemoryStatsCollector cacheWrappers(Collection<GenericCacheWrapper> cacheWrapperMap) {
    this.cacheWrapperMap = cacheWrapperMap;
    return this;
  }

  public void run() {
    try {
      while (running) {
        logger.info("----------- Memory -----------");

        // Get non-duplicate set of the ehcaches

        for (GenericCacheWrapper cacheWrapper : cacheWrapperMap) {
          cacheWrapper.logMemoryInfo(logger);
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
