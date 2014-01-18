package org.terracotta.ehcache.testing.driver;

import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

public interface CacheDriver extends Runnable {

//  static final String UPDATE_RATIO = "update";
//  static final String REMOVE_RATIO = "remove";
//  static final String GET_RATIO = "get";
//  static final String PUT_RATIO = "put";
//  static final String PUTIFABSENT_RATIO = "putIfAbsent";

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
