package org.terracotta.ehcache.testing.statistics.logger;

import java.util.Set;

import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.statistics.StatsNode;

public interface StatsLogger {

  void log(StatsNode node);

  void logMainHeader(final Set<CacheWrapper> cacheWrapperMap, final String[] titles);
}
