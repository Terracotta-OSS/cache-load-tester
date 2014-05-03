package org.terracotta.ehcache.testing.statistics.logger;

import au.com.bytecode.opencsv.CSVWriter;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CsvStatsLoggerImpl implements StatsLogger {

  private CSVWriter logger;
  private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public CsvStatsLoggerImpl(String file) {
    Writer writer;
    try {
      File myFile = new File(file);
      File parentDir = myFile.getParentFile();
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }
      writer = new BufferedWriter(new FileWriter(myFile));
      System.out.println(myFile.getAbsolutePath());
      this.logger = new CSVWriter(writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void header() {
    logger.writeNext(new String[] {
        "Date", "Cache", "Txn_Count", "TPS", "Avg_Lat", "Min_Lat", "Max_Lat" });
  }

  public void log(final StatsNode node) {
    Map<String, Stats> readStatsList = node.getReadStatsList();
    Map<String, Stats> writeStatsList = node.getWriteStatsList();

    logNodeStats(readStatsList, writeStatsList);
  }

  private void logNodeStats(final Map<String, Stats> readStatsList,
                            final Map<String, Stats> writeStatsList) {

    List<String> statsList = new ArrayList<String>();
    statsList.add(df.format(Calendar.getInstance().getTime()));

    for (final String name : readStatsList.keySet()) {
      Stats readStats, writeStats, stat;

      readStats = readStatsList.get(name).getPeriodStats();
      writeStats = writeStatsList.get(name).getPeriodStats();

      stat = new Stats(readStats).add(writeStats);

      statsList.add("" + stat.getTxnCount());
      statsList.add("" + stat.getThroughput());
      statsList.add("" + stat.getAvgLatency());
      statsList.add("" + stat.getMinLatency());
      statsList.add("" + stat.getMaxLatency());
    }
    logToCSV(statsList.toArray(new String[statsList.size()]));
  }

  public void logMainHeader(final Collection<GenericCacheWrapper> cacheWrapperMap, final String[] titles) {
    List<String> headers = new ArrayList<String>();
    headers.add("TimeStamp");
    for (GenericCacheWrapper cache : cacheWrapperMap) {
      for (String title : titles)
        headers.add(cache.getName() + "_" + title);
    }
    logToCSV(headers.toArray(new String[headers.size()]));
  }

  private void logToCSV(String[] items) {
    try {
      logger.writeNext(items);
      logger.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
