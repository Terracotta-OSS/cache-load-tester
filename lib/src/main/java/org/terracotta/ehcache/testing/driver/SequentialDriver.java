package org.terracotta.ehcache.testing.driver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

public class SequentialDriver implements CacheDriver {

  private final List<? extends CacheDriver> drivers;

  public static CacheDriver inSequence(CacheDriver... drivers) {
    return new SequentialDriver(Arrays.asList(drivers));
  }

  public SequentialDriver(List<CacheDriver> drivers) {
    this.drivers = drivers;
  }

  public void run() {
    for (CacheDriver d : drivers) {
      d.run();
    }
  }

  public StatsNode getFinalStatsNode() {
		Iterator<? extends CacheDriver> iterator = drivers.iterator();
		if (iterator.hasNext())
			return iterator.next().getFinalStatsNode();
		return null;
  }

}
