package org.terracotta.ehcache.testing;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
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
import org.terracotta.ehcache.testing.driver.SequentialDriver;
import org.terracotta.ehcache.testing.driver.ParallelDriver.PooledException;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.validator.Validation;

public class ValidatingCacheTest {

  @Test
  public void testEqualsValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testEqualsValidation").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).sequentially().untilFilled();
      CacheDriver access = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.GAUSSIAN, 0, 1000, 10).validate().stopAfter(10, TimeUnit.SECONDS);
      SequentialDriver.inSequence(load, access).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testParallelEqualsValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testParallelEqualsValidation").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver load = CacheLoader.load(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).sequentially().untilFilled();
      CacheDriver access = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.GAUSSIAN, 0, 1000, 10).validate().stopAfter(10, TimeUnit.SECONDS);
      SequentialDriver.inSequence(load, ParallelDriver.inParallel(4, access)).run();
      Assert.assertTrue(one.getSize() > 0);
    } finally {
      manager.shutdown();
    }
  }

  @Test
  public void testFailingValidation() {
    CacheManager manager = new CacheManager(new Configuration().name("testFailingValidation").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver access = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.GAUSSIAN, 0, 1000, 10).validateUsing(new FailingValidation()).stopAfter(10, TimeUnit.SECONDS);
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
    CacheManager manager = new CacheManager(new Configuration().name("testFailingParallelValidation").maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(new CacheConfiguration("default", 0)));
    try {
      Ehcache one = manager.addCacheIfAbsent("one");
      CacheDriver access = CacheAccessor.access(one).using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128)).atRandom(Distribution.GAUSSIAN, 0, 1000, 10).validateUsing(new FailingValidation()).stopAfter(10, TimeUnit.SECONDS);
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

    public void validate(int seed, Object value) {
      throw new AssertionError();
    }
  }
}
