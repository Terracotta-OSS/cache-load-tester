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
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.driver.ParallelDriver;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;
import org.terracotta.ehcache.testing.statistics.logger.CsvStatsLoggerImpl;
import org.terracotta.ehcache.testing.termination.TimedTerminationCondition;

/**
 * Made by aurbrsz / 10/24/11 - 22:59
 */
public class SimpleAccessWithStatsTest {

  @Test
  public void testCsvStats() {
    CacheManager manager = new CacheManager(new Configuration().name("testCsvStats")
        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    Ehcache cache1 = manager.addCacheIfAbsent("cache1");
    Ehcache cache2 = manager.addCacheIfAbsent("cache2");

    CacheAccessor access = CacheAccessor.access(cache1, cache2)
        .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(300, 1200))
        .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000).updateRatio(0.02)
        .terminateOn(new TimedTerminationCondition(30, TimeUnit.SECONDS)).enableStatistics(true)
        .addLogger(new CsvStatsLoggerImpl("target/logs-example.csv"));

    ParallelDriver.inParallel(4, access).run();

    long filesize = new File("target/logs-example.csv").length();
    Assert.assertTrue("CSV file should not be empty", filesize > 0);

    manager.shutdown();
  }

  @Test
  public void testConsoleStats() {
    CacheManager manager = new CacheManager(new Configuration().name("testCsvStats")
        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    Ehcache cache1 = manager.addCacheIfAbsent("cache1");
    Ehcache cache2 = manager.addCacheIfAbsent("cache2");

    CacheLoader loader = CacheLoader.load(cache1, cache2)
        .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(100, 400))
        .enableStatistics(true).sequentially()
        .addLogger(new ConsoleStatsLoggerImpl())
        .iterate(32000);
    loader.run();


    CacheAccessor access = CacheAccessor.access(cache1, cache2)
        .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(300, 1200))
        .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000).updateRatio(0.02)
        .terminateOn(new TimedTerminationCondition(30, TimeUnit.SECONDS)).enableStatistics(true)
        .addLogger(new CsvStatsLoggerImpl("target/logs-example2.csv"))
        .addLogger(new ConsoleStatsLoggerImpl());

    ParallelDriver.inParallel(4, access).run();


    manager.shutdown();
  }

}
