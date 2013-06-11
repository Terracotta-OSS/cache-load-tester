package org.terracotta.ehcache.testing.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;

public class ParallelDriver implements CacheDriver {

  private static final Logger log = LoggerFactory.getLogger(ParallelDriver.class);
  private final Collection<? extends CacheDriver> drivers;
  private final long threadGroupShutdownTimeout = Long.parseLong(System.getProperty("threadgroup.shutdown.timeout", "120000"));
  private final long parallelDriverThreadSleep = Long.parseLong(System.getProperty("parallel.driver.thread.sleep", "2000"));
  

  public static CacheDriver inParallel(int count, CacheDriver job) {
    return new ParallelDriver(Collections.nCopies(count, job));
  }

  public static CacheDriver inParallel(CacheDriver... drivers) {
    return new ParallelDriver(Arrays.asList(drivers));
  }

  public ParallelDriver(Collection<? extends CacheDriver> drivers) {
    this.drivers = drivers;
  }

public void run() {
    DriverThreadGroup group = new DriverThreadGroup();
    try {
      Collection<Thread> threads = new ArrayList<Thread>(drivers.size());
      for (CacheDriver driver : drivers) {
        threads.add(new Thread(group, driver, "ParallelDriver-Thread-" + driver.getClass().getSimpleName()));
      }
      for (Thread t : threads) {
        t.start();
      }

      boolean interrupted = false;
      try {
        for (Thread t : threads) {
          while (t.isAlive()) {
            try {
              t.join();
            } catch (InterruptedException e) {
              interrupted = true;
              e.printStackTrace();
            }
          }
        }
      } finally {
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    } finally {
      try {
    	  long totalSleepCount = threadGroupShutdownTimeout / parallelDriverThreadSleep;
    	  if(totalSleepCount < 1){
    		  totalSleepCount = 1;
    	  }
    	  long sleepCount = 0;
        while (group.activeCount() != 0 && sleepCount < totalSleepCount) {
          log.warn("Shutting down Thread group...");
          Thread[] list = new Thread[group.activeCount()];
          group.enumerate(list);

          for (Thread t : list) {
            log.debug("================ " + t.getName() + " =================");
            for (StackTraceElement s : t.getStackTrace())
              log.debug("{}", s);
          }
          group.interrupt();
          Thread.sleep(parallelDriverThreadSleep);
          sleepCount++;
          group.interrupt();
        }
        
        if(sleepCount >= totalSleepCount){
        	log.info("Timeout: "+ threadGroupShutdownTimeout +" ms occurred while waiting for thread group to shutdown");
        	Thread[] list = new Thread[group.activeCount()];
        	group.enumerate(list);
        	
        	for(Thread t : list){
        		log.debug("======="+t.getName()+"=======");
        		for(StackTraceElement s : t.getStackTrace()){
        			log.debug("{}", s);
        		}
        	}
        }
        
        group.destroy();
      } catch (Exception e) {
        e.printStackTrace();
      }
      log.warn("ThreadGroup shutdown complete.");
    }
    PooledException pooled = group.getPooledException();
    if (pooled != null) {
      throw pooled;
    }
  }

  public static class PooledException extends RuntimeException {

    private static final long serialVersionUID = -2151936042723864607L;

    private final Map<Thread, Throwable> causes;
    private final String NEW_LINE = System.getProperty("line.separator");

    public PooledException(Map<Thread, Throwable> causes) {
      if (causes.size() == 1) {
        initCause(causes.values().iterator().next());
      }

      this.causes = causes;
    }

    public Map<Thread, Throwable> getCauses() {
      return Collections.unmodifiableMap(causes);
    }

    @Override
    public String getMessage() {
      StringBuilder sb = new StringBuilder();

      sb.append("WARNING (QA ehcache-test-loading project) - This is a composite StackTrace holding all Thread StackTraces: \n");
      for (Map.Entry<Thread, Throwable> entry : causes.entrySet()) {
        sb.append("Throwable exception").append(NEW_LINE)
            .append(entry.getKey().getName()).append(NEW_LINE)
            .append(entry.getValue().getMessage()).append(NEW_LINE)
            .append("   ----------   ");

      }
      return sb.toString();
    }

    @Override
    public String getLocalizedMessage() {
      StringBuilder sb = new StringBuilder();

      sb.append("WARNING (QA ehcache-test-loading project) - This is a composite StackTrace holding all Thread StackTraces: \n");
      for (Map.Entry<Thread, Throwable> entry : causes.entrySet()) {
        sb.append("Throwable exception").append(NEW_LINE)
            .append(entry.getKey().getName()).append(NEW_LINE)
            .append(entry.getValue().getLocalizedMessage()).append(NEW_LINE)
            .append("   ----------   ");
      }
      return sb.toString();
    }

  }

  static class DriverThreadGroup extends ThreadGroup {

    private final Map<Thread, Throwable> failures = Collections.synchronizedMap(new IdentityHashMap<Thread, Throwable>());

    public DriverThreadGroup() {
      super("ParallelDriver");
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
      failures.put(thread, throwable);
    }

    public PooledException getPooledException() {
      if (failures.isEmpty()) {
        return null;
      } else {
        for (Throwable tw : failures.values())
          tw.printStackTrace();
        return new PooledException(failures);
      }
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
