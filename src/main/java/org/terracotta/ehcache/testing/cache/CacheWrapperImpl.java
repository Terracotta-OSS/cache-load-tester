package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;

import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsReporter;

public class CacheWrapperImpl implements CacheWrapper {

	private static final int KB = 1024;
	private final Stats readStats;
  protected final Stats writeStats;

	protected final Ehcache cache;
	protected boolean statistics = false;

  /**
	 * Implementation of {@link CacheWrapper}
	 *
	 * @param cache
	 *            the underlying Ehcache
	 */
	public CacheWrapperImpl(final Ehcache cache) {
		this.cache = cache;
		readStats = new Stats();
		writeStats = new Stats();
	}

	public void put(Object key, Object value) {
		long start = (statistics) ? now() : 0;
		try {
        cache.put(new Element(key, value));
    } catch (NonStopCacheException nsce) {
			writeStats.incrementNonstopExceptionCount();
		}
		long end = (statistics) ? now() : 0;
		if (statistics) {
			writeStats.add(end - start);
		}
	}

	public Object get(Object key) {
		long start = (statistics) ? now() : 0;
		try {
			Element e = cache.get(key);
			long end = (statistics) ? now() : 0;
			if (statistics) {
				readStats.add(end - start);
			}
			if (e == null) {
				return null;
			}
			return e.getObjectValue();
		} catch (NonStopCacheException nsce) {
			readStats.incrementNonstopExceptionCount();
		}
		return null;
	}

	/**
	 * Enables statistics collection and registers cache to
	 * {@link StatsReporter}
	 */
	public void setStatisticsEnabled(boolean statistics) {
		this.statistics = statistics;
		if (statistics)
			StatsReporter.getInstance().register(this.cache, this);
	}

	protected static long now() {
		return System.currentTimeMillis();
	}

	public Long getSize() {
		//TODO: check cache.getSize() to return long instead
		return (long) cache.getSize();
	}

	public String getName() {
		return cache.getName();
	}

	public Stats getReadStats() {
		return readStats;
	}

	public Stats getWriteStats() {
		return writeStats;
	}

	public void resetStats() {
		readStats.reset();
		writeStats.reset();
	}

	public long getOffHeapSize() {
		try {
			return cache.getStatistics().getLocalOffHeapSizeInBytes() / KB;
        } catch (NonStopCacheException nsce) {
            return -1;
		} catch (NoSuchMethodError e) {
			return -1;
		} catch (UnsupportedOperationException e) {
			return -1;
		}
	}

	public long getOnDiskSize() {
		try {
			return cache.getStatistics().getLocalDiskSizeInBytes() / KB;
		} catch (UnsupportedOperationException e) {
			return -1;
		} catch (NonStopCacheException nsce) {
	         return -1;
		} catch (NoSuchMethodError e) {
			return -1;
		}
	}

	public long getOnHeapSize() {
		try {
			return cache.getStatistics().getLocalHeapSizeInBytes() / KB;
		} catch (NoSuchMethodError e) {
			return -1;
        } catch (NonStopCacheException nsce) {
            return -1;
		} catch (UnsupportedOperationException e) {
			return -1;
		}
	}

  public void remove(Object key) {
    cache.remove(key);
  }

	public Ehcache getCache() {
		return cache;
	}

}
