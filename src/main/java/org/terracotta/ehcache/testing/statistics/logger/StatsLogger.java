package org.terracotta.ehcache.testing.statistics.logger;

import net.sf.ehcache.Ehcache;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.statistics.StatsNode;

import java.util.Map;

public interface StatsLogger {

  void log(StatsNode node);

  void logMainHeader(final Map<Ehcache, CacheWrapper> cacheWrapperMap, final String[] titles);
}
