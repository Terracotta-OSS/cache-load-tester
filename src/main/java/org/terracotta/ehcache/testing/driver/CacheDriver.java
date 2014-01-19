package org.terracotta.ehcache.testing.driver;

import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

public interface CacheDriver extends Runnable {

  enum OPERATION {
    STRICT_GET, GET, UPDATE, REMOVE, PUT, PUTIFABSENT;
  }

  /**
	 * @deprecated use getFinalStatsNode() to get collective stats of
	 *             reads/writes/overall
	 * @return Overall Final Stats
	 */
	@Deprecated
	public Stats getFinalStats();

	/**
	 *
	 * @return Node Stats which contains reads/writes/overall stats
	 */
	public StatsNode getFinalStatsNode();

}
