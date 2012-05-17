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
      CacheDriver load = CacheLoader.load(cache)
          .using(StringGenerator.integers(),
              new ObjectGenerator() {
                public Object generate(final int seed) {
                  return new MyClass(seed, getName(seed), getEmail(seed));
                }

                private String getEmail(final int seed) {
                  return seed + "@" + "mail.com";
                }

                private String getName(final int seed) {
                  return "MyName " + seed;
                }
              }
          )
          .sequentially().iterate(nbIterations);
      load.run();
      Assert.assertTrue(cache.getSize() == nbIterations);
      for (int i = 0; i < nbIterations; i++) {
        System.out.println(cache.get("" + i).getValue().toString());
      }
    } finally {
      manager.shutdown();
    }

  }

  public class MyClass implements Serializable {

    private int id;
    private String name;
    private String email;

    public MyClass(final int id, final String name, final String email) {
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
