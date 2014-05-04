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
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.validator.Validation;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import static org.terracotta.EhcacheOperation.get;
import static org.terracotta.EhcacheOperation.put;
import static org.terracotta.EhcacheOperation.putIfAbsent;
import static org.terracotta.EhcacheOperation.remove;
import static org.terracotta.EhcacheWrapper.ehcache;

public class CasOperationsTest {

  @Test
  public void testMultipleOperations() {
    CacheManager manager = new CacheManager(new Configuration().name("testSimpleLoad")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");

      System.out.println("cache size = " + one.getSize());
      CacheDriver load = CacheLoader.load(ehcache(one)).doOps(put(0.50), putIfAbsent(0.50))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).sequentially().untilFilled();
      load.run();
      System.out.println("cache size = " + one.getSize());

      CacheDriver access = CacheAccessor.access(ehcache(one)) //CACHES.ehcache( )   / CACHES.jsr107( )
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
