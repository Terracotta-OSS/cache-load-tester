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
package org.terracotta.ehcache.testing.objectgenerator;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;

import java.io.Serializable;

import junit.framework.Assert;

import static org.terracotta.EhcacheWrapper.ehcache;

/**
 * @author Aurelien Broszniowski
 */
public class GenericGeneratorTest {

  @Test
  public void testGenericGenerator() {
    CacheManager manager = new CacheManager(
        new Configuration().name("testSimpleLoad").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
            .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache cache = manager.addCacheIfAbsent("someCache");
      final int nbIterations = 100;
      CacheDriver load = CacheLoader.load(ehcache(cache))
          .using(StringGenerator.integers(),
              new ObjectGenerator() {
                public Object generate(final long seed) {
                  return new MyClass(seed, getName(seed), getEmail(seed));
                }

                private String getEmail(final long seed) {
                  return seed + "@" + "mail.com";
                }

                private String getName(final long seed) {
                  return "MyName " + seed;
                }
              }
          )
          .sequentially().iterate(nbIterations);
      load.run();
      Assert.assertTrue(cache.getSize() == nbIterations);
      for (int i = 0; i < nbIterations; i++) {
        System.out.println(cache.get("" + i).getObjectKey().toString());
      }
    } finally {
      manager.shutdown();
    }

  }

  public class MyClass implements Serializable {

    private final long id;
    private final String name;
    private final String email;

    public MyClass(final long id, final String name, final String email) {
      this.id = id;
      this.name = name;
      this.email = email;
    }

    @Override
    public String toString() {
      return "MyClass id=" + id + ", name=" + name + ", email=" + email;
    }
  }
}
