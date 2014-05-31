/*
 *  Copyright Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.terracotta.ehcache.testing;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Assert;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;

import java.util.concurrent.TimeUnit;

import static org.terracotta.EhcacheWrapper.ehcache;


/**
 * Made by aurbrsz / 7/5/11 - 12:21
 */
public class SimpleMultipleCacheTest {

  @Test
  public void testSimpleLoad() {
    CacheManager manager =
        new CacheManager(new Configuration().name("testSimpleLoad").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
            .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      Ehcache two = manager.addCacheIfAbsent("two");
      Ehcache three = manager.addCacheIfAbsent("three");

      CacheDriver load = CacheLoader.load(ehcache(one, two, three))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .sequentially()
          .untilFilled();
      load.run();
      Assert.assertTrue(one.getSize() > 0);
      Assert.assertTrue(two.getSize() > 0);
      Assert.assertTrue(three.getSize() > 0);
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
      Ehcache two = manager.addCacheIfAbsent("two");
      Ehcache three = manager.addCacheIfAbsent("three");

      CacheAccessor accessor = CacheAccessor.access(ehcache(one))
          .andAccess(ehcache(two))
          .andAccess(ehcache(three))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 100000, 1000)
          .stopAfter(10, TimeUnit.SECONDS);
      accessor.run();
      Assert.assertTrue(one.getSize() > 0);
      Assert.assertTrue(two.getSize() > 0);
      Assert.assertTrue(three.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testSimpleAccessorArray() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleAccessorArray")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));

    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      Ehcache two = manager.addCacheIfAbsent("two");
      Ehcache three = manager.addCacheIfAbsent("three");

      CacheAccessor accessor = CacheAccessor.access(ehcache(one, two, three))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 100000, 1000)
          .stopAfter(10, TimeUnit.SECONDS);
      accessor.run();
      Assert.assertTrue(one.getSize() > 0);
      Assert.assertTrue(two.getSize() > 0);
      Assert.assertTrue(three.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }
}
