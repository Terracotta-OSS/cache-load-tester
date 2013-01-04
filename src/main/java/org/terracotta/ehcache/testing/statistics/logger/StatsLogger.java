package org.terracotta.ehcache.testing.statistics.logger;

import java.util.Collection;

import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.statistics.StatsNode;

public interface StatsLogger {

  void log(StatsNode node);

  void logMainHeader(final Collection<CacheWrapper> cacheWrapperMap, final String[] titles);
}
