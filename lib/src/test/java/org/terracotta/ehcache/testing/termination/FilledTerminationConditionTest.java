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
package org.terracotta.ehcache.testing.termination;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;

import static org.terracotta.ehcache.testing.cache.CACHES.ehcache;


public class FilledTerminationConditionTest {
  private CacheManager cacheManager;

  @Before
  public void setUp() {
    Configuration configuration = new Configuration().name("FilledTerminationConditionTest")
        .defaultCache(new CacheConfiguration("defaultCache", 100))
        .cache(
            new CacheConfiguration("cache1", 100)
        )
        .cache(
            new CacheConfiguration("cache2", 200)
        );
    cacheManager = new CacheManager(configuration);
  }

  @After
  public void teardown() {
    cacheManager.shutdown();
  }

  @Test
  public void testMultiplesTestWithFirstFilledBeforeOthers() {
    Cache cache1 = cacheManager.getCache("cache1");
    Cache cache2 = cacheManager.getCache("cache2");

    CacheDriver cacheDriver = CacheLoader.load(ehcache(cache1, cache2))
        .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(1000))
        .sequentially()
        .untilFilled();
    cacheDriver.run();

    org.junit.Assert.assertEquals("First cache should be filled", 100, cache1.getSize());
    org.junit.Assert.assertEquals("Second cache should be filled", 200, cache2.getSize());
  }
}
