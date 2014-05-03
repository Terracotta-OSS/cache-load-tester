package org.terracotta.ehcache.testing.statistics;

import java.io.File;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;

import org.junit.Assert;
import org.junit.Test;
import org.terracotta.ehcache.testing.cache.CACHES;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.driver.ParallelDriver;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;
import org.terracotta.ehcache.testing.statistics.logger.CsvStatsLoggerImpl;
import org.terracotta.ehcache.testing.termination.TimedTerminationCondition;

import static org.terracotta.ehcache.testing.operation.EhcacheOperation.*;

public class SimpleAccessWithStatsTest {

  @Test
  public void testCsvStats() {
    CacheManager manager = new CacheManager(new Configuration().name("testCsvStats")
        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    Ehcache cache1 = manager.addCacheIfAbsent("cache1");
    Ehcache cache2 = manager.addCacheIfAbsent("cache2");

    CacheLoader loader = CacheLoader.load(CACHES.ehcache(cache1, cache2))
        .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(800, 1200))
        .enableStatistics(true).sequentially()
        .addLogger(new CsvStatsLoggerImpl("target/logs-example-load.csv"))
        .untilFilled();
    ParallelDriver.inParallel(4, loader).run();

    CacheAccessor access = CacheAccessor.access(CACHES.ehcache(cache1, cache2))
        .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(300, 1200))
        .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000).doOps(update(0.02))
        .terminateOn(new TimedTerminationCondition(20, TimeUnit.SECONDS)).enableStatistics(true)
        .addLogger(new CsvStatsLoggerImpl("target/logs-example-access.csv"));
    ParallelDriver.inParallel(4, access).run();

    Assert.assertTrue("Load CSV file should not be empty", new File("target/logs-example-load.csv").length() > 0);
    Assert.assertTrue("Access CSV file should not be empty", new File("target/logs-example-access.csv").length() > 0);

    manager.shutdown();
  }

  @Test
  public void testConsoleStats() {
    CacheManager manager = new CacheManager(new Configuration().name("testConsoleStats")
        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    Ehcache cache1 = manager.addCacheIfAbsent("cache1");
    Ehcache cache2 = manager.addCacheIfAbsent("cache2");

    CacheLoader loader = CacheLoader.load(CACHES.ehcache(cache1, cache2))
     .doOps(putIfAbsent(1.0))
        .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(800, 1200))
        .enableStatistics(true).sequentially()
        .addLogger(new ConsoleStatsLoggerImpl())
        .untilFilled();
    ParallelDriver.inParallel(4, loader).run();

    CacheAccessor access = CacheAccessor.access(CACHES.ehcache(cache1, cache2))
         .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(800, 1200))
         .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000).doOps(update(0.2), remove(0.2), putIfAbsent(0.6))
         .terminateOn(new TimedTerminationCondition(20, TimeUnit.SECONDS)).enableStatistics(true)
         .addLogger(new ConsoleStatsLoggerImpl());

    ParallelDriver.inParallel(4, access).run();

    manager.shutdown();
  }

}
