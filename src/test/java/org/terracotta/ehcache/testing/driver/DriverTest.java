package org.terracotta.ehcache.testing.driver;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Test;
import org.terracotta.ehcache.testing.cache.CacheWrapperImpl;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import static com.jayway.awaitility.Awaitility.await;
import static org.terracotta.ehcache.testing.cache.CACHES.ehcache;

/**
 * @author Aurelien Broszniowski
 */
public class DriverTest {

  @Test
  public void testParallelShutdown() throws Exception {
    final CacheManager manager = new CacheManager(
        new Configuration().name("testEqualsDriver")
            .cache(new CacheConfiguration().name("parallel").maxBytesLocalHeap(64, MemoryUnit.MEGABYTES))
    );
    Cache parallel = manager.getCache("parallel");
    CacheDriver loadParallel = CacheLoader.load(ehcache(parallel))
        .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
        .sequentially()
        .untilFilled();
    final CacheDriver driver = ParallelDriver.inParallel(4, loadParallel);

    await().atMost(30, TimeUnit.SECONDS).until(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        driver.run();
        return true;
      }
    });
    manager.shutdown();

  }

  @Test
  public void testParallelAndSequentialAreEquivalent() {
    CacheManager manager = new CacheManager(
        new Configuration().name("testEqualsDriver")
            .cache(new CacheConfiguration().name("sequential").maxBytesLocalHeap(64, MemoryUnit.MEGABYTES))
            .cache(new CacheConfiguration().name("parallel").maxBytesLocalHeap(64, MemoryUnit.MEGABYTES))
    );

    Cache sequential = manager.getCache("sequential");
    Cache parallel = manager.getCache("parallel");

    CacheDriver loadSequential = CacheLoader.load(ehcache(sequential))
        .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
        .sequentially()
        .untilFilled();
    SequentialDriver.inSequence(loadSequential).run();
    long seqHeapSize = sequential.getStatistics().getLocalHeapSize();
    long seqOffHeapSize = sequential.getStatistics().getLocalOffHeapSize();
    long seqDiskSize = sequential.getStatistics().getLocalDiskSize();

    CacheDriver loadParallel = CacheLoader.load(ehcache(parallel))
        .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
        .sequentially()
        .untilFilled();
    ParallelDriver.inParallel(4, loadParallel).run();
    long parHeapSize = parallel.getStatistics().getLocalHeapSize();
    long parOffHeapSize = parallel.getStatistics().getLocalOffHeapSize();
    long parDiskSize = parallel.getStatistics().getLocalDiskSize();

    Assert.assertEquals(seqHeapSize, parHeapSize);
    Assert.assertEquals(seqOffHeapSize, parOffHeapSize);
    Assert.assertEquals(seqDiskSize, parDiskSize);

    manager.shutdown();
  }

}
