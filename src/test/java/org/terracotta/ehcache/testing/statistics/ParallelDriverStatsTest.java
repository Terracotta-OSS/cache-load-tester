package org.terracotta.ehcache.testing.statistics;

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
import org.terracotta.ehcache.testing.objectgenerator.ByteArrayGenerator;
import org.terracotta.ehcache.testing.objectgenerator.StringGenerator;
import org.terracotta.ehcache.testing.statistics.logger.ConsoleStatsLoggerImpl;
import org.terracotta.ehcache.testing.termination.IterationTerminationCondition;

public class ParallelDriverStatsTest {
	@Test
	public void testMultiCacheSequentialLoading() {
		CacheManager manager = new CacheManager(new Configuration()
				.name("testMultiCacheSequentialLoading")
				.maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
				.defaultCache(new CacheConfiguration("default", 0)));

		Ehcache cache1 = manager.addCacheIfAbsent("cache1");
		Ehcache cache2 = manager.addCacheIfAbsent("cache2");

		int size = 100000;

		CacheLoader loader = CacheLoader
				.load(cache1, cache2)
				.using(StringGenerator.integers(),
						ByteArrayGenerator.fixedSize(10))
				.enableStatistics(true).sequentially().iterate(size)
				.logUsing(new ConsoleStatsLoggerImpl());
		loader.run();
		Assert.assertEquals(size, cache1.getSize());
		Assert.assertEquals(size, cache2.getSize());

		int threads = 5;
		int perThread = size / threads;
		CacheAccessor[] accessors = new CacheAccessor[threads];
		for (int i = 0; i < threads; i++) {
			accessors[i] = CacheAccessor
					.access(cache1, cache2)
					.using(StringGenerator.integers(),
							ByteArrayGenerator.randomSize(300, 1200))
					.sequentially(i * perThread)
					.terminateOn(new IterationTerminationCondition(perThread))
					.enableStatistics(true)
					.logUsing(new ConsoleStatsLoggerImpl());
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
	public void testSequentialLoading() {
		CacheManager manager = new CacheManager(new Configuration()
				.name("testSequentialLoading")
				.maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
				.defaultCache(new CacheConfiguration("default", 0)));

		Ehcache cache1 = manager.addCacheIfAbsent("cache1");

		int size = 100000;

		CacheLoader loader = CacheLoader
				.load(cache1)
				.using(StringGenerator.integers(),
						ByteArrayGenerator.fixedSize(10))
				.enableStatistics(true).sequentially().iterate(size)
				.logUsing(new ConsoleStatsLoggerImpl());
		loader.run();
		Assert.assertEquals(size, cache1.getSize());

		int threads = 5;
		int perThread = size / threads;
		CacheAccessor[] accessors = new CacheAccessor[threads];
		for (int i = 0; i < threads; i++) {
			accessors[i] = CacheAccessor
					.access(cache1)
					.using(StringGenerator.integers(),
							ByteArrayGenerator.randomSize(300, 1200))
					.sequentially(i * perThread)
					.terminateOn(new IterationTerminationCondition(perThread))
					.enableStatistics(true)
					.logUsing(new ConsoleStatsLoggerImpl());
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
