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
package org.terracotta.ehcache.testing.statistics;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Before;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.ParallelDriver;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;
import org.terracotta.ehcache.testing.termination.TimedTerminationCondition;

import java.util.concurrent.TimeUnit;

import static org.terracotta.ehcache.testing.cache.CACHES.ehcache;
import static org.terracotta.ehcache.testing.operation.EhcacheOperation.update;

/**
 * Made by aurbrsz / 10/28/11 - 18:19
 */
public class EquivalentCachesStats {

  @Before
  public void setUp() throws InterruptedException {
    Thread.currentThread().join(30000);
  }
  
  /**
   * This is a test to measure performances on a count based cache between 2.4 and 2.5
   * This does not need to be included in the automatic jenkins jobs
   */
  @Test
  public void testPerfsOnCountBasedCacheUnclustered() {
    Configuration configuration = new Configuration()
        .defaultCache(new CacheConfiguration().name("defaultCache")
            .maxEntriesLocalHeap(0))
        .cache(
            new CacheConfiguration().name("unclusteredCountBased")
                .maxEntriesLocalHeap(0)
        );
    CacheManager manager = new CacheManager(configuration);

    accessAndStats(manager.getCache("unclusteredCountBased"));

    System.out.println("unclustered=" + manager.getCache("unclusteredCountBased").getStatistics().getLocalHeapSizeInBytes());

    manager.shutdown();
  }

  @Test
  public void testPerfsOnSizeBasedCacheUnclustered() {
    waitForProfiler(false);

    Configuration configuration = new Configuration()
        .defaultCache(new CacheConfiguration().name("defaultCache")
            .maxBytesLocalHeap(350, MemoryUnit.MEGABYTES))
        .cache(
            new CacheConfiguration().name("unclusteredSizeBased")
                .maxBytesLocalHeap(350, MemoryUnit.MEGABYTES)
        );
    CacheManager manager = new CacheManager(configuration);

    accessAndStats(manager.getCache("unclusteredSizeBased"));

    System.out.println("unclustered=" + manager.getCache("unclusteredSizeBased").getStatistics().getLocalHeapSizeInBytes());

    manager.shutdown();
  }

  private void waitForProfiler(final boolean wait) {
    if (wait) {
      System.out.println("you can attach the profiler");
      try {
        Thread.currentThread().join(TimeUnit.SECONDS.toMillis(20));
      } catch (InterruptedException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
    }
  }

  private void accessAndStats(final Cache cache) {
    CacheAccessor access = CacheAccessor.access(ehcache(cache))
        .enableStatistics(true)
        .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(300, 1200))
        .atRandom(Distribution.GAUSSIAN, 0, 1000000, 100000).doOps(update(0.1))
        .terminateOn(new TimedTerminationCondition(20, TimeUnit.SECONDS))
        .addLogger(new ConsoleStatsLoggerImpl());

    ParallelDriver.inParallel(4, access).run();

  }
}
