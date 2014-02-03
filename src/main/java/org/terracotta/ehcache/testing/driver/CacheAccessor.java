package org.terracotta.ehcache.testing.driver;

import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.driver.AccessPattern.Pattern;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.operation.CacheOperation;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;
import org.terracotta.ehcache.testing.statistics.StatsReporter;
import org.terracotta.ehcache.testing.statistics.logger.StatsLogger;
import org.terracotta.ehcache.testing.termination.TerminationCondition;
import org.terracotta.ehcache.testing.validator.Validation;

import java.util.concurrent.TimeUnit;

public abstract class CacheAccessor implements CacheDriver {
  private final StatsReporter reporter = StatsReporter.getInstance();

  protected boolean statistics = false;

  public static CacheAccessor access(Ehcache... caches) {
    CacheAccessor accessor = new IndividualCacheAccessor(caches[0]);

    for (int i = 1, cachesLength = caches.length; i < cachesLength; i++) {
      final Ehcache cache = caches[i];
      accessor = accessor.andAccess(cache);
    }
    return accessor;
  }

  /**
   * Add ehcache to be accessed
   *
   * @param one Ehcache
   * @return this
   */
  public static CacheAccessor access(Ehcache one) {
    return new IndividualCacheAccessor(one);
  }

  /**
   * Add access to multiple caches
   *
   * @param one
   * @return this
   */
  public abstract CacheAccessor andAccess(Ehcache one);

  /**
   * Access the caches sequentially
   *
   * @return this
   */
  public abstract CacheAccessor sequentially();

  public abstract CacheAccessor sequentially(long offset);

  /**
   * Add thinktime between each request
   *
   * @param micros thinktime in microseconds
   * @return this
   */
  public abstract CacheAccessor addThinkTime(long micros);

  /**
   * Sets weight for the current {@link IndividualCacheAccessor}<br>
   * Will be ignored if {@link #accessPattern(Pattern, int, int)} is set
   *
   * @param i
   * @return
   */
  public abstract CacheAccessor withWeight(int i);

  /**
   * Add {@link ObjectGenerator} to be used while accessing.
   *
   * @param integers
   * @param fixedSize
   * @return this
   */
  public abstract CacheAccessor using(ObjectGenerator integers, ObjectGenerator fixedSize);

  public abstract CacheAccessor atRandom(Distribution normal, long min, long max, long width);

  /**
   * stop the test after specified time
   *
   * @param time
   * @param unit
   * @return this
   */
  public abstract CacheAccessor stopAfter(int time, TimeUnit unit);

  /**
   * Execute till the cache is full.
   *
   * @return this
   */
  public abstract CacheAccessor untilFilled();

  /**
   * Terminate when {@link TerminationCondition} is met.
   *
   * @param termination
   * @return
   */
  public abstract CacheAccessor terminateOn(TerminationCondition termination);

  public abstract CacheAccessor validate();

  public abstract CacheAccessor validate(final Validation.Mode validationMode);

  public abstract CacheAccessor validateUsing(Validation validation);

  public abstract CacheAccessor validateUsing(final Validation.Mode validationMode, Validation validation);

  /**
   * Enable statistics collection
   *
   * @param statistics
   * @return this
   */
  public abstract CacheAccessor enableStatistics(boolean statistics);

  /**
   * Sets access pattern for the {@link MultipleCacheAccessor}
   *
   * @param pattern  {@link Pattern} type
   * @param duration durationInSecs for transition (SPIKE,WAVE)
   * @param interval interval in secs
   * @return this
   */
  public abstract CacheAccessor accessPattern(Pattern pattern, int duration, int interval);

  /**
   * Add operations that we want to execute, with their ratio
   *
   * @param cacheOperations an array of Operations
   * @return this
   */
  public abstract CacheAccessor doOps(CacheOperation... cacheOperations);

  protected abstract void execute();

  public void run() {
    init();
    startReporting();
    execute();
    stopReporting();
  }

  /**
   * Initialize the accessor (e.g. calculate ratios of operations)
   *
   */
  protected abstract void init();

  /**
   * @param loggers
   * @return CacheAccessor
   * @deprecated Use addLogger instead
   */
  @Deprecated
  public CacheAccessor logUsing(StatsLogger... loggers) {
    reporter.logUsing(loggers);
    return this;
  }

  public CacheAccessor addLogger(StatsLogger logger) {
    reporter.addLogger(logger);
    return this;
  }

  public Stats getFinalStats() {
    return reporter.getFinalStats().getOverallStats();
  }

  public StatsNode getFinalStatsNode() {
    if (!statistics)
      throw new IllegalStateException("Statistics are not enabled!");
    return reporter.getFinalStats();
  }

  static long now() {
    return System.currentTimeMillis();
  }

  protected void startReporting() {
    if (statistics)
      reporter.startReporting();
  }

  protected void stopReporting() {
    if (statistics)
      reporter.stopReporting();
  }


}
