package org.jsoftbiz;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.junit.Assert;
import org.junit.Test;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.jsoftbiz.MemcachedWrapper.memcached;

/**
 * Test load/access on Memcached single cache
 *
 * @author Aurelien Broszniowski
 */

public class MemcachedSingleCacheTest {

  @Test
  public void testSimpleLoad() throws IOException, InterruptedException, MemcachedException, TimeoutException {
    MemcachedClient client = new XMemcachedClient("localhost", 11211);

    try {
      GenericCacheWrapper cacheWrapper = memcached(client);
      client.flushAll();

      System.out.println("size = " + cacheWrapper.getSize());

      CacheDriver load = CacheLoader.load(cacheWrapper)
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .sequentially()
          .untilFilled();
      load.run();
      Assert.assertTrue(cacheWrapper.getSize() > 0);
    } finally {
      client.shutdown();
    }

  }

}
