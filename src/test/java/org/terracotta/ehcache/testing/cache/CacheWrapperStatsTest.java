package org.terracotta.ehcache.testing.cache;

import junit.framework.Assert;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;

import org.junit.Test;
import org.terracotta.ehcache.testing.statistics.Stats;

public class CacheWrapperStatsTest {

	@Test
	public void test() {
		CacheManager manager = new CacheManager(new Configuration()
				.maxBytesLocalHeap(16, MemoryUnit.MEGABYTES).defaultCache(
						new CacheConfiguration("default", 0)));
		CacheWrapper test = new CacheWrapperImpl(manager.addCacheIfAbsent("test"));
		test.setStatisticsEnabled(true);

		for (int i = 0; i < 1000; i++) {
			test.put(i, new Object());
			test.get(i);
		}
		Stats read = test.getReadStats();
		Stats write = test.getWriteStats();
		Assert.assertEquals(read.getTxnCount(), 1000);
		Assert.assertEquals(write.getTxnCount(), 1000);
		Assert.assertTrue("Max Latency != -1", read.getMaxLatency() != -1);
		Assert.assertTrue("Min Latency != -1", read.getMinLatency() != -1);

		Stats readPeriod = read.getPeriodStats();
		Stats writePeriod = write.getPeriodStats();
		Assert.assertNotNull(readPeriod);
		Assert.assertNotNull(writePeriod);
		Assert.assertEquals(readPeriod.getTxnCount(), 1000);
		Assert.assertEquals(writePeriod.getTxnCount(), 1000);

		readPeriod = read.getPeriodStats();
		writePeriod = write.getPeriodStats();
		Assert.assertEquals(readPeriod.getTxnCount(), 0);
		Assert.assertEquals(writePeriod.getTxnCount(), 0);

		for (int i = 0; i < 1000; i++) {
			test.put(i, new Object());
			test.get(i);
		}
		readPeriod = read.getPeriodStats();
		writePeriod = write.getPeriodStats();
		Assert.assertEquals(readPeriod.getTxnCount(), 1000);
		Assert.assertEquals(writePeriod.getTxnCount(), 1000);

		Assert.assertEquals(read.getTxnCount(), 2000);
		Assert.assertEquals(write.getTxnCount(), 2000);


	}
}
