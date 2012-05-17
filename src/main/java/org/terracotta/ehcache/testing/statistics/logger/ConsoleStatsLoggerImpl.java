package org.terracotta.ehcache.testing.statistics.logger;

import java.text.NumberFormat;
import java.util.Map;

import net.sf.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

/**
 * Made by aurbrsz / 10/31/11 - 23:46
 */
public class ConsoleStatsLoggerImpl implements StatsLogger {
  private static int detailedMax = Integer.parseInt(System.getProperty("cache.detail.max","4"));
  private static Logger logger = LoggerFactory.getLogger(ConsoleStatsLoggerImpl.class);

  private static final String FORMAT = "%-15s %-7s %12s %10s %10s %10s %10s %10s";
  private static final NumberFormat nf = NumberFormat.getInstance();

  private enum StatsCategory {
    READS, WRITES, TOTAL, NODE
  }

  private void header(final String title, final String... headers) {
    logger.info("==================================================== "
                + title + " =========================================");
    logger.info(String.format(FORMAT, "Cache", "Type", "Txn_Count", "TPS",
        "Avg_Lat", "Min_Lat", "Max_Lat", "NonstopExceptionCount"));
    logger.info("========================================================"
                + "==================================================");
  }

  private void logStats(Stats stat, String name, StatsCategory type) {
    logger.info(String.format(FORMAT, name,
        type, nf.format(stat.getTxnCount()),
        nf.format(stat.getThroughput()),
        nf.format(stat.getAvgLatency()),
        nf.format(stat.getMinLatency()),
        nf.format(stat.getMaxLatency()),
		nf.format(stat.getNonstopExceptionCount())));
  }

  public void log(final StatsNode node) {
    Map<String, Stats> readStatsList;
    Map<String, Stats> writeStatsList;

    readStatsList = node.getReadStatsList();
    writeStatsList = node.getWriteStatsList();
    logNodeStats("Period Stats", readStatsList, writeStatsList);

    readStatsList = node.getCumulativeReadStatsList();
    writeStatsList = node.getCumulativeWriteStatsList();
    logNodeStats("Cumulative Stats", readStatsList, writeStatsList);
  }

 private void logNodeStats(String title, final Map<String, Stats> readStatsList,
                           final Map<String, Stats> writeStatsList) {
    header(title);
    Stats nodeTotal = new Stats();
    Stats readTotal = new Stats();
    Stats writeTotal = new Stats();

    boolean isDetailed = true;
    if (readStatsList.keySet().size() > detailedMax){
    	isDetailed = false;
    }
    for (final String name : readStatsList.keySet()) {

      Stats readStats = readStatsList.get(name);
      Stats writeStats = writeStatsList.get(name);
      Stats total = new Stats(readStats).add(writeStats);
      if (isDetailed){
    	logStats(readStats, name, StatsCategory.READS);
      	logStats(writeStats, name, StatsCategory.WRITES);
      	logStats(total, name, StatsCategory.TOTAL);
      	logger.info("");
      }
      readTotal.add(readStats);
      writeTotal.add(writeStats);
      nodeTotal.add(total);
    }
    logStats(readTotal, "All Caches", StatsCategory.READS);
    logStats(writeTotal, "All Caches", StatsCategory.WRITES);
    logStats(nodeTotal, "All Caches", StatsCategory.TOTAL);
    logger.info("All Caches Histogram: \n" + nodeTotal.getHisto().toString());
  }

	    /*
	==================================================== Period Stats =========================================
	Cache           Type       Txn_Count        TPS    Avg_Lat    Min_Lat    Max_Lat NonstopExceptionCount
	==========================================================================================================
	All Caches      READS        379,753     94,677      0.085          0         77          0
	All Caches      WRITES       191,432     47,726      0.161          0         87          0
	All Caches      TOTAL        571,185    142,404      0.111          0         87          0
	All Caches Histogram:
	0-10 frequency count = 40621373 percentage = 100.0
	10-50 frequency count = 16407 percentage = 0.0
	50-100 frequency count = 2499 percentage = 0.0
	100-200 frequency count = 59 percentage = 0.0
	200-500 frequency count = 0 percentage = 0.0
	500-1000 frequency count = 0 percentage = 0.0
	1000-5000 frequency count = 141 percentage = 0.0
	5000-PLUS frequency count = 0 percentage = 0.0

	==================================================== Cumulative Stats =========================================
	Cache           Type       Txn_Count        TPS    Avg_Lat    Min_Lat    Max_Lat NonstopExceptionCount
	==========================================================================================================
	All Caches      READS     26,996,736     91,045      0.092          0      2,428          0
	All Caches      WRITES    13,643,710     46,012      0.167          0      2,452          0
	All Caches      TOTAL     40,640,446    137,058      0.117          0      2,452          0
	All Caches Histogram:
	0-10 frequency count = 1507526960 percentage = 100.0
	10-50 frequency count = 618063 percentage = 0.0
	50-100 frequency count = 80409 percentage = 0.0
	100-200 frequency count = 2272 percentage = 0.0
	200-500 frequency count = 0 percentage = 0.0
	500-1000 frequency count = 0 percentage = 0.0
	1000-5000 frequency count = 4632 percentage = 0.0
	5000-PLUS frequency count = 0 percentage = 0.0
	    */

  public void logMainHeader(final Map<Ehcache, CacheWrapper> cacheWrapperMap, final String[] titles) {
    logger.info("---------------------------- starting to log performances ----------------------------");
  }
}
