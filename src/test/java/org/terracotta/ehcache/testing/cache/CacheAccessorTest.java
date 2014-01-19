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
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

public class CacheAccessorTest {

  @Test(expected = RuntimeException.class)
  public void testIncorrectRatios() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleAccessor")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheAccessor accessor = CacheAccessor.access(one)
          .putIfAbsent(0.20).put(0.20).get(0.20).update(0.20).remove(0.21)
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 100000, 1000)
          .stopAfter(10, TimeUnit.SECONDS);
      accessor.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }
}
