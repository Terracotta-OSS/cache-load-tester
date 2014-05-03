package org.terracotta.ehcache.testing.statistics.logger;

import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.statistics.StatsNode;

import java.util.Collection;

public interface StatsLogger {

  void log(StatsNode node);

  void logMainHeader(final Collection<GenericCacheWrapper> cacheWrapperMap, final String[] titles);
}
