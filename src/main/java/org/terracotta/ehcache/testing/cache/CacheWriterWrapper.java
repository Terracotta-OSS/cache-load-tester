package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;
import org.terracotta.ehcache.testing.statistics.Stats;

/**
 * @author Aurelien Broszniowski
 */
public class CacheWriterWrapper extends CacheWrapperImpl {
  /**
   * Implementation of {@link org.terracotta.ehcache.testing.cache.CacheWrapper}
   *
   * @param cache the underlying Ehcache
   */
  public CacheWriterWrapper(final Ehcache cache) {
    super(cache);
  }

  @Override
  public void put(final Object key, final Object value) {
    System.out.println("------------ PUT");
    long start = (statistics) ? now() : 0;
    try {
      cache.putWithWriter(new Element(key, value));
    } catch (NonStopCacheException nsce) {
      writeStats.incrementNonstopExceptionCount();
    }
    long end = (statistics) ? now() : 0;
    if (statistics) {
      writeStats.add(end - start);
    }
  }
}
