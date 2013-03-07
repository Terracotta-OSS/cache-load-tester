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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;
import org.terracotta.ehcache.testing.statistics.logger.CsvStatsLoggerImpl;
import org.terracotta.ehcache.testing.statistics.logger.StatsLogger;

public class StatsReporter {
  private static Logger logger = LoggerFactory.getLogger(StatsReporter.class);
  private static final int reportPeriod = Integer.parseInt(System.getProperty("stats.reporter.interval","4"));

  private final Map<String, CacheWrapper> cacheWrappers = new HashMap<String, CacheWrapper>();

  private final AtomicReference<Thread> reportThread = new AtomicReference<Thread>();
  private final AtomicReference<Thread> memoryReportThread = new AtomicReference<Thread>();
  private final Set<StatsLogger> statsLoggers = new HashSet<StatsLogger>();
  private final AtomicInteger curr = new AtomicInteger();
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
   * In older versions, we had a console logger included and an option Csv logger {@link CsvStatsLoggerImpl} implementing @StatsLogger
   * Now we can choose what kind of logger we want, {@link CsvStatsLoggerImpl} or {@link ConsoleStatsLoggerImpl}, or even both and
   * we can add as many logger we see fit. Use then {@link #addLogger(StatsLogger)} to add one or multiple loggers
   *
   * @param loggers array of Stats Logger implementing {@link StatsLogger}
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
	logger.debug("Starting stats reporting ... thread #" + curr.incrementAndGet());
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
//			e.printStackTrace();
        	logger.debug("StatsReporter interrupted.");
            return;
          }
          doReport();
        }
      }
    };
    reportThread.set(t);

    Thread m = new Thread(new MemoryStatsCollector(cacheWrappers.values()));
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
	int last = curr.decrementAndGet();
	logger.debug("Trying to stop stats reporting ... thread #" + last);
	if (last != 0)
		return;

    Thread t = reportThread.get();
    if (t == null)
      return;
    try {
      Thread m = memoryReportThread.get();
      if (m != null) {
        m.interrupt();
        m.join();
      }
      t.interrupt();
      t.join();
      finalise();
      doEndReport();
    } catch (InterruptedException e) {
//			e.printStackTrace();
    }
    reportThread.set(null);
    memoryReportThread.set(null);
  }

  public synchronized void register(Ehcache cache, CacheWrapper cacheWrapper) {
    String name = cacheWrapper.getName();
    logger.info(name + " registered for stats reporting.");
    cacheWrappers.put(name, cacheWrapper);
    node.addReadStats(name, cacheWrapper.getReadStats());
    node.addWriteStats(name, cacheWrapper.getWriteStats());
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
  private synchronized void resetStats() {
	node.reset();
    for (CacheWrapper cache : cacheWrappers.values()) {
      cache.resetStats();
    }
  }

  private void logMainHeader() {
    for (StatsLogger statsLogger : statsLoggers) {
      statsLogger.logMainHeader(cacheWrappers.values(), StatsElement.names());
    }
  }

  /**
   * Logs periodic stats to the list of {@link StatsLogger}.
   */
  private void doReport() {
    if (statsLoggers.size() == 0) {
      logger.warn("You didn't set any StatsLogger to log the stats. You can set one or more using: logUsing(StatsLogger... loggers)");
    }
    for (StatsLogger statsLogger : statsLoggers)
      statsLogger.log(node);
  }

  private void doEndReport() {
    for (StatsLogger statsLogger : statsLoggers)
      statsLogger.log(node);
  }
}
