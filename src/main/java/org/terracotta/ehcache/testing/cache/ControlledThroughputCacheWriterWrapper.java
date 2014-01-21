package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;

/**
 * @author Vivek Verma
 */
public class ControlledThroughputCacheWriterWrapper extends CacheWrapperImpl {
  /**
   * Implementation of {@link org.terracotta.ehcache.testing.cache.CacheWrapper}
   *
   * @param cache the underlying Ehcache
   */
	
  private static int tpsThreshold = Integer.getInteger("tpsThreshold", -1);
  
  public ControlledThroughputCacheWriterWrapper(final Ehcache cache) {
    super(cache);
  }

  @Override
  public void put(final Object key, final Object value) {
    if (tpsThreshold != -1) {
      while (writeStats.getThroughput() > tpsThreshold) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    long start = (statistics) ? now() : 0;
    try {
      cache.put(new Element(key, value));
    } catch (NonStopCacheException nsce) {
      writeStats.incrementTotalExceptionCount();
    }
    long end = (statistics) ? now() : 0;
    if (statistics) {
      writeStats.add(end - start);
    }
  }
}
