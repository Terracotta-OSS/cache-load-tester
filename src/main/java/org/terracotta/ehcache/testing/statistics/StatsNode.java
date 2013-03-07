package org.terracotta.ehcache.testing.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Made by aurbrsz / 11/1/11 - 1:09
 */
public class StatsNode {

  private final Map<String, Stats> readStatsList = new LinkedHashMap<String, Stats>();
  private final Map<String, Stats> writeStatsList = new LinkedHashMap<String, Stats>();

  private Stats overallStats;

  public void addReadStats(final String name, final Stats read) {
	readStatsList.put(name, read);
  }

  public void addWriteStats(final String name, final Stats write) {
	writeStatsList.put(name, write);
  }

  public synchronized void reset() {
    for (Stats s : readStatsList.values())
        s.reset();
    for (Stats s : writeStatsList.values())
        s.reset();
    overallStats = new Stats();
  }

  public Map<String, Stats> getReadStatsList() {
    return readStatsList;
  }

  public Map<String, Stats> getWriteStatsList() {
    return writeStatsList;
  }

  public Stats getOverallStats() {
	  if (overallStats == null)
		  throw new IllegalStateException("StatsNode needs to be finalized!");
    return overallStats;
  }

  @Deprecated
  public Stats getFinalStats(){
    return getOverallStats();
  }

  public Stats getOverallReadStats(){
	  Stats overall = new Stats();
	  for (Stats read: getReadStatsList().values())
		  overall.add(read);
	  return overall;
  }

  public Stats getOverallWriteStats(){
	  Stats overall = new Stats();
	  for (Stats write: getWriteStatsList().values())
		  overall.add(write);
	  return overall;
  }

  @Override
  public String toString(){
	  return getOverallStats().toString();
  }

  public void finalise(){
	  overallStats = new Stats();
	  for (Stats stat: getReadStatsList().values()){
		  stat.finalise();
		  overallStats.add(stat);
	  }
	  for (Stats stat: getWriteStatsList().values()){
		  stat.finalise();
		  overallStats.add(stat);
	  }
	  overallStats.finalise();
  }

}
