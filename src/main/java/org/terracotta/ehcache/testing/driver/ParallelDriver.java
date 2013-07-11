package org.terracotta.ehcache.testing.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ParallelDriver implements CacheDriver {

  private static final Logger log = LoggerFactory.getLogger(ParallelDriver.class);
  private final Collection<? extends CacheDriver> drivers;
  private final ExecutorService executorService;

  public static CacheDriver inParallel(int count, CacheDriver job) {
    return new ParallelDriver(Collections.nCopies(count, job));
  }

  public static CacheDriver inParallel(CacheDriver... drivers) {
    return new ParallelDriver(Arrays.asList(drivers));
  }

  public ParallelDriver(Collection<? extends CacheDriver> drivers) {
    this.drivers = drivers;
    this.executorService = Executors.newFixedThreadPool(drivers.size());
  }

  public void run() {
    Map<Future, Throwable> causes = new HashMap<Future, Throwable>();
    try {
      List<Future> futures = new ArrayList<Future>();
      for (CacheDriver driver : drivers) {
        futures.add(executorService.submit(driver));
      }
      for (Future future : futures) {
        try {
          future.get();
        } catch (ExecutionException e) {
          causes.put(future, e.getCause());
          // This is a timeout of 60 seconds before shutting down in case of an exception
          Thread.sleep(60000);
          for (Future futureToCancel : futures) {
            if (!futureToCancel.isDone())
              futureToCancel.cancel(true);
          }
        }
      }
      executorService.shutdown();
      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    } finally {
      if (!causes.isEmpty()) {
        for (Throwable tw : causes.values()){
          tw.printStackTrace();
        }
        throw new PooledException(causes);
      }
    }
  }

  public static class PooledException extends RuntimeException {

    private final Map<Future, Throwable> causes;
    private final String NEW_LINE = System.getProperty("line.separator");

    public PooledException(Map<Future, Throwable> causes) {
      if (causes.size() == 1) {
        initCause(causes.values().iterator().next());
      }
      this.causes = causes;
    }

    public Map<Future, Throwable> getCauses() {
      return Collections.unmodifiableMap(causes);
    }

    @Override
    public String getMessage() {
      StringBuilder sb = new StringBuilder();

      sb.append("WARNING (QA ehcache-test-loading project) - This is a composite StackTrace holding all Thread StackTraces: \n");
      for (Map.Entry<Future, Throwable> entry : causes.entrySet()) {
        sb.append("Throwable exception").append(NEW_LINE)
            .append(entry.getKey().toString()).append(NEW_LINE)
            .append(entry.getValue().getMessage()).append(NEW_LINE)
            .append("   ----------   ");

      }
      return sb.toString();
    }

    @Override
    public String getLocalizedMessage() {
      StringBuilder sb = new StringBuilder();

      sb.append("WARNING (QA ehcache-test-loading project) - This is a composite StackTrace holding all Thread StackTraces: \n");
      for (Map.Entry<Future, Throwable> entry : causes.entrySet()) {
        sb.append("Throwable exception").append(NEW_LINE)
            .append(entry.getKey().toString()).append(NEW_LINE)
            .append(entry.getValue().getLocalizedMessage()).append(NEW_LINE)
            .append("   ----------   ");
      }
      return sb.toString();
    }

  }

  /**
   * @see CacheDriver
   */
  @Deprecated
  public Stats getFinalStats() {
    Iterator<? extends CacheDriver> iterator = drivers.iterator();
    if (iterator.hasNext())
      return iterator.next().getFinalStats();
    return null;
  }

  public StatsNode getFinalStatsNode() {
    Iterator<? extends CacheDriver> iterator = drivers.iterator();
    if (iterator.hasNext())
      return iterator.next().getFinalStatsNode();
    return null;
  }
}
