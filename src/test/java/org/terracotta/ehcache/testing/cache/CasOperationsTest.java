package org.terracotta.ehcache.testing.cache;


import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.validator.Validation;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import static org.terracotta.ehcache.testing.operation.EhcacheOperation.get;
import static org.terracotta.ehcache.testing.operation.EhcacheOperation.remove;

public class CasOperationsTest {

  @Test
  public void testMultipleOperations() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleLoad")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");

      System.out.println("cache size = " + one.getSize());
      CacheDriver load = CacheLoader.load(one).put(0.50).putIfAbsent(0.50)
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).sequentially().untilFilled();
      load.run();
      System.out.println("cache size = " + one.getSize());

      CacheDriver access = CacheAccessor.access(one) //CACHES.ehcache( )   / CACHES.jsr107( )
          .doOps(get(0.75), remove(0.05))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validate(Validation.Mode.UPDATE)
          .stopAfter(10, TimeUnit.SECONDS);
      access.run();
      System.out.println("cache size = " + one.getSize());

      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }

  }
}
