/*
 * All content copyright (c) Terracotta, Inc., except as may otherwise be noted in a separate copyright notice. All
 * rights reserved.
 */
package org.terracotta.ehcache.testing.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.statistics.logger.StatsLogger;

public class StatsReporter {
  private static Logger logger = LoggerFactory.getLogger(StatsReporter.class);
  private static final int reportPeriod = Integer.parseInt(System.getProperty("stats.reporter.interval","4"));

  private final Map<Ehcache, CacheWrapper> cacheWrapperMap = new HashMap<Ehcache, CacheWrapper>();

  private final AtomicReference<Thread> reportThread = new AtomicReference<Thread>();
  private final AtomicReference<Thread> memoryReportThread = new AtomicReference<Thread>();
  private final Set<StatsLogger> statsLoggers = new HashSet<StatsLogger>();
  private final StatsNode node = new StatsNode();

  private enum StatsElement {

    TXN_COUNT("Txn_Count"), TPS("TPS"),
    AVG_LAT("Avg_Lat"), MIN_LAT("Min_Lat"), MAX_LAT("Max_Lat"),
    HEAP("Heap(MB)"), OFFHEAP("Offheap(MB)"), DISK("Disk(MB)");

    private final String title;

    private StatsElement(final String title) {
      this.title = title;
    }

    public static String[] names() {
      return new String[] { TXN_COUNT.name(), TPS.name(), AVG_LAT.name(), MIN_LAT.name(),
          MAX_LAT.name(), HEAP.name(), OFFHEAP.name(), DISK.name()
      };
    }
  }

  private static class StatsReporterHolder {
    public static final StatsReporter instance = new StatsReporter();

  }

  public static StatsReporter getInstance() {
    return StatsReporterHolder.instance;
  }

  /**
   * Add a logger for stats.
   * In older versions, we had a console logger included and an option Csv logger @CsvStatsLoggerImpl implementing @StatsLogger
   * Now we can choose what kind of logger we want, @CsvStatsLoggerImpl or @ConsoleStatsLoggerImpl, or even both and
   * we can add as many logger we see fit. Use then @addLogger(StatsLogger logger) to add one or multiple loggers
   *
   * @param loggers array of Stats Logger implementing @StatsLogger
   * @return StatsReporter
   */
  public synchronized StatsReporter logUsing(StatsLogger... loggers) {
    for (StatsLogger logger : loggers) {
      addLogger(logger);
    }
    return this;
  }

  /**
   * Also logs the periodic stats using {@link StatsLogger}
   *
   * @param logger StatsLogger
   * @return this
   */
  public synchronized StatsReporter addLogger(StatsLogger logger) {
    statsLoggers.add(logger);
    return this;
  }

  /**
   * Starts statistics reporting. Resets all previous stats, if any.
   *
   * @return this
   */
  public synchronized StatsReporter startReporting() {
    if (reportThread.get() != null) {
      return this;
    }

    Thread t = new Thread() {
      @Override
      public void run() {

        logMainHeader();
        resetStats();
        while (true) {
          try {
            TimeUnit.SECONDS.sleep(reportPeriod);
          } catch (InterruptedException e) {
//						e.printStackTrace();
            return;
          }
          doReport();
        }
      }
    };
    reportThread.set(t);

    Thread m = new Thread(new MemoryStatsCollector(cacheWrapperMap));
    memoryReportThread.set(m);

    t.start();
    m.start();
    return this;
  }

  /**
   * Stops reporter thread. Clears the list of registered cacheWrapper since
   * new {@link CacheWrapper} is created for each driver
   */
  public synchronized void stopReporting() {
    Thread t = reportThread.get();
    if (t == null)
      return;
    try {
      // TODO : Add finalise on MemoryReportThread
      finalise();
      Thread m = memoryReportThread.get();
      if (m != null) {
        m.interrupt();
        m.join();
      }
      t.interrupt();
      t.join();

      doReport();
    } catch (InterruptedException e) {
//			e.printStackTrace();
    }
    reportThread.set(null);
    memoryReportThread.set(null);
  }

  public synchronized void register(Ehcache cache, CacheWrapper cacheWrapper) {
    logger.debug(cache.getName() + " registered for stats reporting.");
    cacheWrapperMap.put(cache, cacheWrapper);
  }

  public StatsNode getFinalStats() {
    return node;
  }

  private void finalise() {
	node.finalise();
    statsLoggers.clear();
  }

  /**
   * Reset stats for all {@link CacheWrapper}
   */
  private void resetStats() {
	node.reset();
    for (CacheWrapper cache : cacheWrapperMap.values()) {
      cache.resetStats();
    }
  }

  private void logMainHeader() {
    for (StatsLogger statsLogger : statsLoggers) {
      statsLogger.logMainHeader(cacheWrapperMap, StatsElement.names());
    }
  }

  /**
   * Logs periodic stats to the list of {@link StatsLogger}.
   */
  private void doReport() {
    for (CacheWrapper cache : cacheWrapperMap.values()) {
      node.addReadStats(cache.getName(), cache.getReadStats().getPeriodStats());
      node.addWriteStats(cache.getName(), cache.getWriteStats().getPeriodStats());
    }

    if (statsLoggers.size() == 0) {
      logger.warn("You didn't set any StatsLogger to log the stats. You can set one or more using: logUsing(StatsLogger... loggers)");
    }
    for (StatsLogger statsLogger : statsLoggers) {
      statsLogger.log(node);
    }
  }
}
