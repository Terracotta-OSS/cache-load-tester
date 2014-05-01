package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;
import net.sf.ehcache.constructs.nonstop.RejoinCacheException;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsReporter;

public class CacheWrapperImpl implements CacheWrapper {

	private static final int KB = 1024;
	private final Stats readStats;
  protected final Stats writeStats;
  protected final Stats removeStats;

	protected final Ehcache cache;
	protected boolean statistics = false;

  /**
	 * Implementation of {@link CacheWrapper}
	 *
	 * @param cache the underlying Ehcache
	 */
	public CacheWrapperImpl(final Ehcache cache) {
		this.cache = cache;
		readStats = new Stats();
		writeStats = new Stats();
		removeStats = new Stats();
	}

  @Override
	public void put(Object key, Object value) {
    long start = (statistics) ? now() : 0;
    try {
      cache.put(new Element(key, value));
    } catch (NonStopCacheException nsce) {
      writeStats.incrementTotalExceptionCount();
    } catch (RejoinCacheException rce) {
      writeStats.incrementTotalExceptionCount();
    }
    long end = (statistics) ? now() : 0;
    if (statistics) {
      writeStats.add(end - start);
    }
  }

  @Override
  public void putWithWriter(final Object key, final Object value) {
    long start = (statistics) ? now() : 0;
    try {
      cache.putWithWriter(new Element(key, value));
    } catch (RejoinCacheException rce) {
      writeStats.incrementTotalExceptionCount();
    } catch (NonStopCacheException nsce) {
      writeStats.incrementTotalExceptionCount();
    }
    long end = (statistics) ? now() : 0;
    if (statistics) {
      writeStats.add(end - start);
    }
  }

  @Override
	public Object putIfAbsent(Object key, Object value) {
    long start = (statistics) ? now() : 0;
    Element element = null;
    try {
      element = cache.putIfAbsent(new Element(key, value));
    } catch (NonStopCacheException nsce) {
      writeStats.incrementTotalExceptionCount();
    } catch (RejoinCacheException rce) {
      writeStats.incrementTotalExceptionCount();
    }
    long end = (statistics) ? now() : 0;
    if (statistics) {
      writeStats.add(end - start);
    }
    return element;
  }

  @Override
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
      readStats.incrementTotalExceptionCount();
    } catch (RejoinCacheException rce) {
      readStats.incrementTotalExceptionCount();
    }

    return null;
  }

  public boolean remove(Object key) {
    boolean removed = false;
    long start = (statistics) ? now() : 0;
    try {
      removed = cache.remove(key);
      long end = (statistics) ? now() : 0;
      if (statistics) {
        removeStats.add(end - start);
      }
    } catch (NonStopCacheException nsce) {
      removeStats.incrementTotalExceptionCount();
    } catch (RejoinCacheException rce) {
      removeStats.incrementTotalExceptionCount();
    }
    return removed;
  }

  @Override
  public boolean removeElement(final Object key, final Object value) {
    boolean removed = false;
    long start = (statistics) ? now() : 0;
    try {
      removed = cache.removeElement(new Element(key, value));
      long end = (statistics) ? now() : 0;
      if (statistics) {
        removeStats.add(end - start);
      }
    } catch (NonStopCacheException nsce) {
      removeStats.incrementTotalExceptionCount();
    } catch (RejoinCacheException rce) {
      removeStats.incrementTotalExceptionCount();
    }
    return removed;
  }

  @Override
  public Element replace(final Object key, Object newValue) {
    long start = (statistics) ? now() : 0;
    Element element = null;
    try {
      element = cache.replace(new Element(key, newValue));
    } catch (NonStopCacheException nsce) {
      writeStats.incrementTotalExceptionCount();
    } catch (RejoinCacheException rce) {
      writeStats.incrementTotalExceptionCount();
    }
    long end = (statistics) ? now() : 0;
    if (statistics) {
      writeStats.add(end - start);
    }
    return element;
  }

  @Override
  public boolean replaceElement(final Object oldKey, final Object oldValue, final Object newKey, final Object newValue) {
    long start = (statistics) ? now() : 0;
    boolean replaced = false;
    try {
      replaced = cache.replace(new Element(oldKey, oldValue), new Element(newKey, newValue));
    } catch (NonStopCacheException nsce) {
      writeStats.incrementTotalExceptionCount();
    } catch (RejoinCacheException rce) {
      writeStats.incrementTotalExceptionCount();
    }
    long end = (statistics) ? now() : 0;
    if (statistics) {
      writeStats.add(end - start);
    }
    return replaced;
  }

  /**
	 * Enables statistics collection and registers cache to
	 * {@link StatsReporter}
	 */
	public void setStatisticsEnabled(boolean statistics) {
		this.statistics = statistics;
//		if (statistics)
//			StatsReporter.getInstance().register(this.cache, this);
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

  public Stats getRemoveStats() {
    return removeStats;
  }

  public void resetStats() {
		readStats.reset();
		writeStats.reset();
		removeStats.reset();
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

	public Ehcache getCache() {
		return cache;
	}

}
