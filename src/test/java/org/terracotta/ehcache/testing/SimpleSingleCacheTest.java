package org.terracotta.ehcache.testing;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.driver.ParallelDriver;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import static org.terracotta.ehcache.testing.cache.CACHES.ehcache;

public class SimpleSingleCacheTest {

  @Test
  public void testSimpleLoad() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleLoad")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .sequentially()
          .untilFilled();
      load.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testSimplePartitionedLoad() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimplePartitionedLoad")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver partitionedLoad = CacheLoader.load(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .enableStatistics(true).addLogger(new ConsoleStatsLoggerImpl())
          .sequentially().untilFilled().partition(4);
      partitionedLoad.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testSimpleAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleAccessor")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 100000, 1000)
          .stopAfter(10, TimeUnit.SECONDS);
      accessor.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testParallelAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testParallelAccessor")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 100000, 1000)
          .stopAfter(10, TimeUnit.SECONDS);
      ParallelDriver.inParallel(4, accessor).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testLoadUsingAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testLoadUsingAccessor")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.FLAT, 0, Integer.MAX_VALUE, 0)
          .untilFilled();
      accessor.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testLoadUsingParallelAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testLoadUsingParallelAccessor")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.FLAT, 0, Integer.MAX_VALUE, 0)
          .untilFilled();
      ParallelDriver.inParallel(4, accessor).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testFillSingleCache() {
    CacheManager manager = new CacheManager(new Configuration().name("testFillSingleCache")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    Ehcache one = manager.addCacheIfAbsent("one");

    CacheDriver cacheDriver = CacheLoader.load(ehcache(one))
        .using(StringGenerator.integers(), ByteArrayGenerator.collections(1000, 2))
        .sequentially()
        .untilFilled()
        .partition(3);
    cacheDriver.run();
  }
}
