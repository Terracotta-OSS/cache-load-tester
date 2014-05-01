package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.operation.EhcacheOperation;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import static org.terracotta.ehcache.testing.cache.CACHES.ehcache;
import static org.terracotta.ehcache.testing.operation.EhcacheOperation.*;

public class CacheAccessorTest {

  @Test(expected = RuntimeException.class)
  public void testIncorrectRatios() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleAccessor")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(ehcache(one))
          .doOps(putIfAbsent(0.20), put(0.20), get(0.20),
              update(0.20),
              remove(0.21), replaceElement(0.10), replace(0.11),
              removeElement(0.05))
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
  public void testCacheOps() {
    CacheManager manager = new CacheManager(new Configuration().name("testCacheOps")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(ehcache(one)).enableStatistics(true)
          .doOps(putIfAbsent(0.10), put(0.10), update(0.10), remove(0.10),
              replaceElement(0.10), replace(0.10), removeElement(0.05))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 100000, 1000)
          .stopAfter(10, TimeUnit.SECONDS);
      accessor.run();

      Assert.assertTrue(one.getSize() > 0);

      StatsNode node = accessor.getFinalStatsNode();
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


}
