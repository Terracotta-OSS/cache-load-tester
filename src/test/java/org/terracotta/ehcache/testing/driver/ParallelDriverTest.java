package org.terracotta.ehcache.testing.driver;

import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Aurelien Broszniowski
 */

public class ParallelDriverTest {

  @Test
  public void testOneCacheAccessorIsSuccessful() {
    CacheAccessor accessor = mock(CacheAccessor.class);
    ParallelDriver parallelDriver = new ParallelDriver(Arrays.asList(accessor));
    parallelDriver.run();
    verify(accessor, times(1)).run();
  }

  @Test
  public void testTwoCacheAccessorsAreSuccessful() {
    CacheAccessor accessor1 = mock(CacheAccessor.class);
    CacheAccessor accessor2 = mock(CacheAccessor.class);
    ParallelDriver parallelDriver = new ParallelDriver(Arrays.asList(accessor1, accessor2));
    parallelDriver.run();
    verify(accessor1, times(1)).run();
    verify(accessor2, times(1)).run();
  }

  @Test
  public void testOneMultipleCacheAccessorIsSuccessful() {
    IndividualCacheAccessor individualCacheAccessor1 = mock(IndividualCacheAccessor.class);
    IndividualCacheAccessor individualCacheAccessor2 = mock(IndividualCacheAccessor.class);
    MultipleCacheAccessor multipleCacheAccessor = new MultipleCacheAccessor(individualCacheAccessor1, individualCacheAccessor2);
    ParallelDriver parallelDriver = new ParallelDriver(Arrays.asList(multipleCacheAccessor));
    parallelDriver.run();
    verify(individualCacheAccessor1, times(1)).run();
    verify(individualCacheAccessor2, times(1)).run();
  }

  @Test
  public void testTwoMultipleCacheAccessorsAreSuccessful() {
    IndividualCacheAccessor individualCacheAccessor1 = mock(IndividualCacheAccessor.class);
    IndividualCacheAccessor individualCacheAccessor2 = mock(IndividualCacheAccessor.class);
    IndividualCacheAccessor individualCacheAccessor3 = mock(IndividualCacheAccessor.class);
    IndividualCacheAccessor individualCacheAccessor4 = mock(IndividualCacheAccessor.class);
    MultipleCacheAccessor multipleCacheAccessor1 = new MultipleCacheAccessor(individualCacheAccessor1, individualCacheAccessor2);
    MultipleCacheAccessor multipleCacheAccessor2 = new MultipleCacheAccessor(individualCacheAccessor3, individualCacheAccessor4);
    ParallelDriver parallelDriver = new ParallelDriver(Arrays.asList(multipleCacheAccessor1, multipleCacheAccessor2));
    parallelDriver.run();
    verify(individualCacheAccessor1, times(1)).run();
    verify(individualCacheAccessor2, times(1)).run();
    verify(individualCacheAccessor3, times(1)).run();
    verify(individualCacheAccessor4, times(1)).run();
  }

  @Test
  public void testOneCacheAccessorInMultipleParallelIsSuccessful() {
    CacheAccessor accessor = mock(CacheAccessor.class);
    ParallelDriver.inParallel(2, accessor).run();
    verify(accessor, times(2)).run();
  }


  @Test
  public void testOneMultipleCacheAccessorsInMultipleParallelIsSuccessful() {
    IndividualCacheAccessor individualCacheAccessor1 = mock(IndividualCacheAccessor.class);
    IndividualCacheAccessor individualCacheAccessor2 = mock(IndividualCacheAccessor.class);
    MultipleCacheAccessor multipleCacheAccessor = new MultipleCacheAccessor(individualCacheAccessor1, individualCacheAccessor2);
    ParallelDriver.inParallel(2, multipleCacheAccessor).run();

    verify(individualCacheAccessor1, times(2)).run();
    verify(individualCacheAccessor2, times(2)).run();
  }


}
