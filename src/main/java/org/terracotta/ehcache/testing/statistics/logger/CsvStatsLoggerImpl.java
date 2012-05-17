package org.terracotta.ehcache.testing.statistics.logger;

import au.com.bytecode.opencsv.CSVWriter;
import net.sf.ehcache.Ehcache;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CsvStatsLoggerImpl implements StatsLogger {

  private CSVWriter log;
  private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public CsvStatsLoggerImpl(String file) {
    Writer writer;
    try {
      writer = new BufferedWriter(new FileWriter(file));
      this.log = new CSVWriter(writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void log(StatsNode node) {
    Map<String, Stats> readStatsList;
    Map<String, Stats> writeStatsList;

    readStatsList = node.getReadStatsList();
    writeStatsList = node.getWriteStatsList();

    Stats nodeTotal = new Stats();
    for (final String name : readStatsList.keySet()) {
      Stats readStats = readStatsList.get(name);
      Stats writeStats = writeStatsList.get(name);
      Stats total = new Stats(readStats).add(writeStats);
      nodeTotal.add(total);
    }

    List<String> statsList = new ArrayList<String>();
    statsList.add(df.format(Calendar.getInstance().getTime()));
    statsList.add("" + nodeTotal.getTxnCount());
    statsList.add("" + nodeTotal.getThroughput());
    statsList.add("" + nodeTotal.getAvgLatency());
    statsList.add("" + nodeTotal.getMinLatency());
    statsList.add("" + nodeTotal.getMaxLatency());


    logToCSV(statsList.toArray(new String[statsList.size()]));
  }

  public void logMainHeader(final Map<Ehcache, CacheWrapper> cacheWrapperMap, final String[] titles) {
    List<String> headers = new ArrayList<String>();
    headers.add("TimeStamp");
    for (CacheWrapper cache : cacheWrapperMap.values()) {
      for (String title : titles)
        headers.add(cache.getName() + "_" + title);
    }
    logToCSV(headers.toArray(new String[headers.size()]));
  }

  private void logToCSV(String[] items) {
    try {
      log.writeNext(items);
      log.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
