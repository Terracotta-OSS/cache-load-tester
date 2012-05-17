package org.terracotta.ehcache.testing.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Made by aurbrsz / 11/1/11 - 1:09
 */
public class StatsNode {

  private Map<String, Stats> currentReadStatsList = new LinkedHashMap<String, Stats>();
  private Map<String, Stats> currentWriteStatsList = new LinkedHashMap<String, Stats>();

  private Map<String, Stats> currentCumulativeReadStatsList = new LinkedHashMap<String, Stats>();
  private Map<String, Stats> currentCumulativeWriteStatsList = new LinkedHashMap<String, Stats>();

  private Stats overallStats = new Stats();

  public void addReadStats(final String name, final Stats read) {
    currentReadStatsList.put(name, read);
    if (currentCumulativeReadStatsList.get(name) == null) {
      currentCumulativeReadStatsList.put(name, read);
    } else {
      currentCumulativeReadStatsList.get(name).add(read);
    }

    overallStats.add(read);
  }

  public void addWriteStats(final String name, final Stats write) {
    currentWriteStatsList.put(name, write);
    if (currentCumulativeWriteStatsList.get(name) == null) {
      currentCumulativeWriteStatsList.put(name, write);
    } else {
      currentCumulativeWriteStatsList.get(name).add(write);
    }

    overallStats.add(write);
  }

  public void reset() {
    currentReadStatsList = new LinkedHashMap<String, Stats>();
    currentWriteStatsList = new LinkedHashMap<String, Stats>();
    currentCumulativeReadStatsList = new LinkedHashMap<String, Stats>();
    currentCumulativeWriteStatsList = new LinkedHashMap<String, Stats>();
    overallStats = new Stats();
  }

  public Map<String, Stats> getReadStatsList() {
    return currentReadStatsList;
  }

  public Map<String, Stats> getWriteStatsList() {
    return currentWriteStatsList;
  }

  public Map<String, Stats> getCumulativeReadStatsList() {
    return currentCumulativeReadStatsList;
  }

  public Map<String, Stats> getCumulativeWriteStatsList() {
    return currentCumulativeWriteStatsList;
  }

  public Stats getOverallStats() {
    return overallStats;
  }

  @Deprecated
  public Stats getFinalStats(){
    return getOverallStats();
  }

  public Stats getOverallReadStats(){
	  Stats overall = new Stats();
	  for (Stats read: getCumulativeReadStatsList().values())
		  overall.add(read);
	  return overall;
  }

  public Stats getOverallWriteStats(){
	  Stats overall = new Stats();
	  for (Stats write: getCumulativeWriteStatsList().values())
		  overall.add(write);
	  return overall;
  }

  @Override
  public String toString(){
	  return getOverallStats().toString();
  }

  public void finalise(){
	  for (Stats stat: getReadStatsList().values())
		  stat.finalise();
	  for (Stats stat: getWriteStatsList().values())
		  stat.finalise();
	  for (Stats stat: getCumulativeReadStatsList().values())
		  stat.finalise();
	  for (Stats stat: getCumulativeWriteStatsList().values())
		  stat.finalise();
	  overallStats.finalise();
  }

}
