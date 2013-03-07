package org.terracotta.ehcache.testing;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

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

public class SimpleSingleCacheTest {

  /*
  public void stuffToDo() {
    //Reporting
    // - both on caches and cache-drivers
    // run method returning a results object with stats
    // exception handling in drivers
    CacheDriver reporter = CacheReporter.reportOn(one, two).reportOn(accessing).every(10, TimeUnit.SECONDS).to(System.out);
    CacheReporter.report(new MyInformationSource()).to(System.err);
    
    //In case of cluster - split parallel jobs across the active cluster
    ParallelDriver.useCluster();
  }
  */
  
  @Test
  public void testSimpleLoad() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleLoad").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).sequentially().untilFilled();
      load.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }
  
  @Test
  public void testSimplePartitionedLoad() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimplePartitionedLoad").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver partitionedLoad = CacheLoader.load(one)
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .enableStatistics(true).logUsing(new ConsoleStatsLoggerImpl())
          .sequentially().untilFilled().partition(4)
          ;
      partitionedLoad.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }
  
  @Test
  public void testSimpleAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleAccessor").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.GAUSSIAN, 0, 100000, 1000).stopAfter(10, TimeUnit.SECONDS);
      accessor.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }
  
  @Test
  public void testParallelAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testParallelAccessor").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.GAUSSIAN, 0, 100000, 1000).stopAfter(10, TimeUnit.SECONDS);
      ParallelDriver.inParallel(4, accessor).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }
  
  @Test
  public void testLoadUsingAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testLoadUsingAccessor").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.FLAT, 0, Integer.MAX_VALUE, 0).untilFilled();
      accessor.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testLoadUsingParallelAccessor() {
    CacheManager manager = new CacheManager(new Configuration().name("testLoadUsingParallelAccessor").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.FLAT, 0, Integer.MAX_VALUE, 0).untilFilled();
      ParallelDriver.inParallel(4, accessor).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testFillSingleCache() {
    CacheManager manager = new CacheManager(new Configuration().name("testFillSingleCache").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    Ehcache one = manager.addCacheIfAbsent("one");

    CacheDriver cacheDriver = CacheLoader.load(one)
        .using(StringGenerator.integers(), ByteArrayGenerator.collections(1000, 2))
        .sequentially()
        .untilFilled()
        .partition(3);
    cacheDriver.run();
  }
}
