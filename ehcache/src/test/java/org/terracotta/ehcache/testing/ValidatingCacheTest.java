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
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.driver.ParallelDriver;
import org.terracotta.ehcache.testing.driver.ParallelDriver.PooledException;
import org.terracotta.ehcache.testing.driver.SequentialDriver;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.validator.Validation;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import static org.terracotta.EhcacheWrapper.ehcache;

public class ValidatingCacheTest {

  @Test(expected = AssertionError.class)
  public void testEmptyCacheValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testEmptyCacheValidation")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver access = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validate(Validation.Mode.STRICT)
          .stopAfter(10, TimeUnit.SECONDS);
      SequentialDriver.inSequence(access).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testEqualsValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testEqualsValidation")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .sequentially()
          .untilFilled();
      CacheDriver access = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validate(Validation.Mode.STRICT)
          .stopAfter(10, TimeUnit.SECONDS);
      SequentialDriver.inSequence(load, access).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testEqualsObjectValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testEqualsObjectValidation")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration().name("default")));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(ehcache(one)).using(StringGenerator.integers(),
          new ObjectGenerator() {
            @Override
            public Object generate(final long seed) {
              return new TestingObject(seed);
            }
          })
          .sequentially().untilFilled();
      CacheDriver access = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(),
              new ObjectGenerator() {
                @Override
                public Object generate(final long seed) {
                  return new TestingObject(seed);
                }
              })
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validate(Validation.Mode.STRICT)
          .stopAfter(20, TimeUnit.SECONDS);
      SequentialDriver.inSequence(load, access).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test(expected = AssertionError.class)
  public void testNotEqualsObjectValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testNotEqualsObjectValidation")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(ehcache(one)).using(StringGenerator.integers(),
          new ObjectGenerator() {
            @Override
            public Object generate(final long seed) {
              return new TestingObject(seed + 1);
            }
          })
          .sequentially().untilFilled();
      CacheDriver access = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(),
              new ObjectGenerator() {
                @Override
                public Object generate(final long seed) {
                  return new TestingObject(seed);
                }
              })
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validate(Validation.Mode.STRICT)
          .stopAfter(20, TimeUnit.SECONDS);
      SequentialDriver.inSequence(load, access).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testParallelEqualsValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testParallelEqualsValidation")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .sequentially()
          .untilFilled();
      CacheDriver access = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validate(Validation.Mode.STRICT)
          .stopAfter(10, TimeUnit.SECONDS);
      SequentialDriver.inSequence(load, ParallelDriver.inParallel(4, access)).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testFailingValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testFailingValidation")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver access = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validateUsing(new FailingValidation())
          .stopAfter(10, TimeUnit.SECONDS);
      boolean passed = false;
      try {
        access.run();
      } catch (AssertionError e) {
        passed = true;
      }
      if (!passed) {
        Assert.fail("Expected AssertionError");
      }
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testFailingParallelValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testFailingParallelValidation")
        .maxBytesLocalHeap(16, MemoryUnit.MEGABYTES)
        .defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver access = CacheAccessor.access(ehcache(one))
          .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
          .atRandom(Distribution.GAUSSIAN, 0, 1000, 10)
          .validateUsing(new FailingValidation())
          .stopAfter(10, TimeUnit.SECONDS);
      boolean passed = false;
      try {
        ParallelDriver.inParallel(4, access).run();
      } catch (PooledException e) {
        Assert.assertEquals(4, e.getCauses().size());
        for (Throwable t : e.getCauses().values()) {
          Assert.assertEquals(AssertionError.class, t.getClass());
        }
        passed = true;
      }
      if (!passed) {
        Assert.fail("Expected AssertionError");
      }
    } finally {
      manager.shutdown();
    }
  }

  static class FailingValidation implements Validation, Validation.Validator {

    public Validator createValidator(ObjectGenerator valueGenerator) {
      return this;
    }

    public void validate(long seed, Object value) {
      throw new AssertionError();
    }
  }

  public class TestingObject {

    long longNb;
    int intNb;
    String someStr;
    float floatNb;

    public TestingObject(final long seed) {
      Long l = new Long(seed);
      this.longNb = l.longValue();
      this.intNb = l.intValue();
      this.floatNb = l.floatValue();
      this.someStr = "somestring" + l.toString();
    }

    @Override
    public boolean equals(final Object obj) {
      if (!(obj instanceof TestingObject)) {
        return false;
      }
      TestingObject o = (TestingObject)obj;
      return ((getLongNb() == o.getLongNb()) && getFloatNb() == o.getFloatNb()
              && getIntNb() == o.getIntNb() && getSomeStr().equalsIgnoreCase(o.getSomeStr()));
    }

    public long getLongNb() {
      return longNb;
    }

    public int getIntNb() {
      return intNb;
    }

    public String getSomeStr() {
      return someStr;
    }

    public float getFloatNb() {
      return floatNb;
    }
  }
}
