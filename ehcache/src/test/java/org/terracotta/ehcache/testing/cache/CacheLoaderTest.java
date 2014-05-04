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
package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;

import org.junit.Assert;

import static org.terracotta.EhcacheOperation.put;
import static org.terracotta.EhcacheOperation.putIfAbsent;
import static org.terracotta.EhcacheWrapper.ehcache;

public class CacheLoaderTest {

  @Test(expected = RuntimeException.class)
  public void testIncorrectRatios() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleLoad")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(ehcache(one)).doOps(put(0.50), putIfAbsent(0.51))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .sequentially()
          .untilFilled();
      load.run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }

  }
}
