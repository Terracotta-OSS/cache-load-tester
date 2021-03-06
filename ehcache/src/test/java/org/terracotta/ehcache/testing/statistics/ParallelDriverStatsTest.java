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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.terracotta.ehcache.testing.driver.CacheAccessor;
import org.terracotta.ehcache.testing.driver.CacheDriver;
import org.terracotta.ehcache.testing.driver.CacheLoader;
import org.terracotta.ehcache.testing.driver.ParallelDriver;
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;
import org.terracotta.ehcache.testing.termination.IterationTerminationCondition;

import junit.framework.Assert;

import static org.terracotta.EhcacheWrapper.ehcache;

public class ParallelDriverStatsTest {
	@Test
	@Ignore("recheck this test")
	public void testMultiCacheSequentialLoading() {
		CacheManager manager = new CacheManager(new Configuration()
				.name("testMultiCacheSequentialLoading")
				.maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
				.defaultCache(new CacheConfiguration("default", 0)));

		Ehcache cache1 = manager.addCacheIfAbsent("cache1");
		Ehcache cache2 = manager.addCacheIfAbsent("cache2");

		int size = 100000;

    CacheLoader loader = CacheLoader
        .load(ehcache(cache1, cache2))
            .using(StringGenerator.integers(),
                ByteArrayGenerator.fixedSize(10))
            .enableStatistics(true).sequentially().iterate(size)
            .addLogger(new ConsoleStatsLoggerImpl());
    loader.run();
		loader.getFinalStats();
		Assert.assertEquals(size, cache1.getSize());
		Assert.assertEquals(size, cache2.getSize());

		int threads = 5;
		int perThread = size / threads;
		CacheAccessor[] accessors = new CacheAccessor[threads];
		for (int i = 0; i < threads; i++) {
			accessors[i] = CacheAccessor
					.access(ehcache(cache1, cache2))
					.using(StringGenerator.integers(),
							ByteArrayGenerator.randomSize(300, 1200))
					.sequentially(i * perThread)
					.terminateOn(new IterationTerminationCondition(perThread))
					.enableStatistics(true)
					.addLogger(new ConsoleStatsLoggerImpl());
		}
		CacheDriver driver = ParallelDriver.inParallel(accessors);
		driver.run();
		StatsNode node = driver.getFinalStatsNode();
		Stats overall = node.getOverallStats();
		System.err.println("Per thread count : " + perThread);
		System.err.println("Thread count : " + threads);
		Assert.assertEquals(perThread * threads, overall.getTxnCount());
		manager.shutdown();
	}

	@Test
	@Ignore("recheck this test")
	public void testSequentialLoading() {
		CacheManager manager = new CacheManager(new Configuration()
				.name("testSequentialLoading")
				.maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
				.defaultCache(new CacheConfiguration("default", 0)));

		Ehcache cache1 = manager.addCacheIfAbsent("cache1");

		int size = 100000;

		CacheLoader loader = CacheLoader
				.load(ehcache(cache1))
				.using(StringGenerator.integers(),
						ByteArrayGenerator.fixedSize(10))
				.enableStatistics(true).sequentially().iterate(size)
				.addLogger(new ConsoleStatsLoggerImpl());
		loader.run();
		Assert.assertEquals(size, cache1.getSize());

		int threads = 5;
		int perThread = size / threads;
		CacheAccessor[] accessors = new CacheAccessor[threads];
		for (int i = 0; i < threads; i++) {
			accessors[i] = CacheAccessor
					.access(ehcache(cache1))
					.using(StringGenerator.integers(),
							ByteArrayGenerator.randomSize(300, 1200))
					.sequentially(i * perThread)
					.terminateOn(new IterationTerminationCondition(perThread))
					.enableStatistics(true)
					.addLogger(new ConsoleStatsLoggerImpl());
		}
		CacheDriver driver = ParallelDriver.inParallel(accessors);
		driver.run();
		StatsNode node = driver.getFinalStatsNode();
		Stats overall = node.getOverallStats();
		System.err.println("Per thread count : " + perThread);
		System.err.println("Thread count : " + threads);
		Assert.assertEquals(perThread * threads, overall.getTxnCount());
		manager.shutdown();
	}

}
