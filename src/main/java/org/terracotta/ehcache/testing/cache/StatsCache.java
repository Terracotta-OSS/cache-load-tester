package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.Ehcache;

/**
 * Made by aurbrsz / 10/31/11 - 22:33
 */
public class StatsCache extends net.sf.ehcache.constructs.EhcacheDecoratorAdapter {

  public StatsCache(Ehcache underlyingCache) {
    super(underlyingCache);
  }
}
