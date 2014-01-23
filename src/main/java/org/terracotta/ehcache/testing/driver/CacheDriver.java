package org.terracotta.ehcache.testing.driver;

import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

public interface CacheDriver extends Runnable {

  /**
   * GET : @link net.sf.Cache#get(Object)
   * UPDATE : @link net.sf.Cache#put(Element)
   * REMOVE : @link net.sf.Cache#remove(Object)
   * REMOVE_ELEMENT : @link net.sf.Cache#removeElement(Element)
   * REPLACE : @link net.sf.Cache#replace(Element)
   * REPLACE_ELEMENT : @link net.sf.Cache#replace(Element, Element)
   * PUT : @link net.sf.Cache#put(Element)
   * PUT_IF_ABSENT : @link net.sf.Cache#putIfAbsent(Element)
   */
  enum OPERATION {
    STRICT_GET, GET, UPDATE, REMOVE, REMOVE_ELEMENT, REPLACE, REPLACE_ELEMENT, PUT, PUT_IF_ABSENT;
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
