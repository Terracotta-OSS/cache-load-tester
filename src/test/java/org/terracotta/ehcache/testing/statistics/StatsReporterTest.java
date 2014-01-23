package org.terracotta.ehcache.testing.statistics;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.driver.ParallelDriver;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;
import org.terracotta.ehcache.testing.termination.TimedTerminationCondition;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

public class StatsReporterTest {

  @Ignore
  @Test
  public void testStatsReporterShutdown() {
    CacheManager manager = new CacheManager(new Configuration()
        .name("testStatsReporterShutdown")
        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    try {
      Ehcache cache1 = manager.addCacheIfAbsent("cache1");
      Ehcache cache2 = manager.addCacheIfAbsent("cache2");

      CacheLoader loader = CacheLoader
          .load(cache1, cache2)
          .using(StringGenerator.integers(),
              ByteArrayGenerator.randomSize(300, 1200))
          .enableStatistics(true).sequentially().iterate(10000)
          .addLogger(new ConsoleStatsLoggerImpl());

      CacheDriver driver = ParallelDriver.inParallel(8, loader);
      driver.run();
      StatsNode node = driver.getFinalStatsNode();
      Assert.assertEquals(10000, cache1.getSize());
      Assert.assertEquals(10000, cache2.getSize());
      Assert.assertEquals(160000, node.getOverallStats().getTxnCount());
    } finally {
      manager.shutdown();
    }
  }

  // the stats addition is not under synchronization as that will hit the performance
  // will enable it back when copy on read or other technique is implemented.
  @Test
  @Ignore
  public void testStatsNode() {
    CacheManager manager = new CacheManager(new Configuration()
        .name("testStatsNode")
        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    try {
      Ehcache cache1 = manager.addCacheIfAbsent("cache1");
      Ehcache cache2 = manager.addCacheIfAbsent("cache2");

      CacheLoader loader = CacheLoader
          .load(cache1, cache2)
          .using(StringGenerator.integers(),
              ByteArrayGenerator.randomSize(300, 1200))
          .enableStatistics(true).sequentially().iterate(10000)
          .addLogger(new ConsoleStatsLoggerImpl());
      loader.run();
      Assert.assertEquals(10000, cache1.getSize());
      Assert.assertEquals(10000, cache2.getSize());

      CacheAccessor access = CacheAccessor
          .access(cache1, cache2)
          .using(StringGenerator.integers(),
              ByteArrayGenerator.randomSize(300, 1200))
          .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000)
          .update(0.2).remove(0.1)
          .terminateOn(new TimedTerminationCondition(30, TimeUnit.SECONDS))
          .enableStatistics(true).addLogger(new ConsoleStatsLoggerImpl());

      CacheDriver driver = ParallelDriver.inParallel(8, access);
      driver.run();
      StatsNode node = driver.getFinalStatsNode();
      Stats overall = node.getOverallStats();
      Stats read = node.getOverallReadStats();
      Stats write = node.getOverallWriteStats();
      Stats remove = node.getOverallRemoveStats();
        Assert.assertEquals("overall txns should be sum of read, writes and remove",
            overall.getTxnCount(), read.getTxnCount() + write.getTxnCount() + remove.getTxnCount());
        Assert.assertTrue("overall tps should be sum of read, writes and remove",
            Math.abs(overall.getThroughput() / (read.getThroughput() + write.getThroughput() + remove.getThroughput())) == 1);
        Assert.assertEquals("overall min latency should be min of read, writes and remove",
            overall.getMinLatency(), Math.min(remove.getMinLatency(), Math.min(read.getMinLatency(), write.getMinLatency())));
        Assert.assertEquals("overall min latency should be min of read, writes and remove",
            overall.getMaxLatency(), Math.max(remove.getMaxLatency(), Math.max(read.getMaxLatency(), write.getMaxLatency())));
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testNode() {
    CacheManager manager = new CacheManager(new Configuration()
        .name("testNode")
        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    try {
      Ehcache cache1 = manager.addCacheIfAbsent("cache1");
      Ehcache cache2 = manager.addCacheIfAbsent("cache2");

      CacheLoader loader = CacheLoader
          .load(cache1, cache2)
          .using(StringGenerator.integers(),
              ByteArrayGenerator.randomSize(300, 1200))
          .enableStatistics(true).sequentially().iterate(10000)
          .addLogger(new ConsoleStatsLoggerImpl());
      loader.run();
      Assert.assertEquals(10000, cache1.getSize());
      Assert.assertEquals(10000, cache2.getSize());

      for (int i = 0; i < 3; i++) {
        CacheAccessor access = CacheAccessor
            .access(cache1, cache2)
            .using(StringGenerator.integers(),
                ByteArrayGenerator.randomSize(300, 1200))
            .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000)
            .update(0.2)
            .terminateOn(
                new TimedTerminationCondition(10, TimeUnit.SECONDS))
            .enableStatistics(true)
            .addLogger(new ConsoleStatsLoggerImpl());

        CacheDriver driver = ParallelDriver.inParallel(8, access);
        driver.run();
        StatsNode node = driver.getFinalStatsNode();

        System.out.println(node);
      }
    } finally {
      manager.shutdown();
    }
  }

}
