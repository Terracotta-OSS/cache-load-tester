package org.terracotta.ehcache.testing.driver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.cache.CacheWrapperImpl;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.PartitionedSequentialGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.SequenceGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.SequenceGenerator.Sequence;
import org.terracotta.ehcache.testing.sequencegenerator.SequentialSequenceGenerator;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;
import org.terracotta.ehcache.testing.statistics.StatsReporter;
import org.terracotta.ehcache.testing.statistics.logger.StatsLogger;
import org.terracotta.ehcache.testing.termination.FilledTerminationCondition;
import org.terracotta.ehcache.testing.termination.IterationTerminationCondition;
import org.terracotta.ehcache.testing.termination.TerminationCondition;
import org.terracotta.ehcache.testing.termination.TerminationCondition.Condition;

public class CacheLoader implements CacheDriver {
  private static Logger logger = LoggerFactory.getLogger(CacheLoader.class);

  private final Collection<CacheWrapper> caches;

  private Map<OPERATION, Double> ratios = new ConcurrentHashMap<OPERATION, Double>();
  private final Random rnd = new Random();

  private boolean statistics = false;
  private SequenceGenerator sequenceGenerator = null;
  private ObjectGenerator keyGenerator = null;
  private ObjectGenerator valueGenerator = null;
  private TerminationCondition terminationCondition = null;
  private final StatsReporter reporter = StatsReporter.getInstance();

  private CacheLoader(final Class<? extends CacheWrapper> cacheWrapperClass, Ehcache[] caches) throws RuntimeException {
    this.ratios.put(OPERATION.PUT, 0.0);
    this.ratios.put(OPERATION.PUTIFABSENT, 0.0);

    try {
      this.caches = new ArrayList<CacheWrapper>();
      for (Ehcache cache : caches) {
        Constructor constructor = cacheWrapperClass.getDeclaredConstructor(Ehcache.class);
        CacheWrapper wrapper = (CacheWrapper)constructor.newInstance(cache);
        this.caches.add(wrapper);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private CacheLoader(CacheLoader loader, SequenceGenerator sequenceGenerator) {
    this.caches = loader.caches;
    this.keyGenerator = loader.keyGenerator;
    this.valueGenerator = loader.valueGenerator;
    this.terminationCondition = loader.terminationCondition;
    this.sequenceGenerator = sequenceGenerator;
    this.statistics = loader.statistics;
  }

  /**
   * Sets the caches to be loaded. It just assigns doesn't add to list.
   *
   * @param caches
   * @return this
   */
  public static CacheLoader load(Ehcache... caches)  {
    return new CacheLoader(CacheWrapperImpl.class, caches);
  }

  public static CacheLoader load(final Class<? extends CacheWrapper> cacheWrapperClass, Ehcache... caches) {
    return new CacheLoader(cacheWrapperClass, caches);
  }

  public CacheDriver partition(int count) {
    long offset = 0;
    if (sequenceGenerator != null) {
      if (sequenceGenerator instanceof SequentialSequenceGenerator)
        offset = ((SequentialSequenceGenerator)sequenceGenerator).getOffset();
    }
    Collection<CacheDriver> drivers = new ArrayList<CacheDriver>(count);
    for (int i = 0; i < count; i++) {
      drivers.add(new CacheLoader(this,
          new PartitionedSequentialGenerator(offset + i, count)));
    }
    return new ParallelDriver(drivers);
  }

  public CacheLoader put(final double percentage) {
    this.ratios.put(OPERATION.PUT, percentage);
    return this;
  }

  public CacheLoader putIfAbsent(final double percentage) {
    this.ratios.put(OPERATION.PUTIFABSENT, percentage);
    return this;
  }

  /**
     * Sets @ObjectGenerators for keys and values.
     *
     * @param keys
     * @param values
     * @return this
     */
  public CacheLoader using(ObjectGenerator keys, ObjectGenerator values) {
    if (keyGenerator == null) {
      keyGenerator = keys;
    } else {
      throw new IllegalStateException(
          "Key ObjectGenerator already chosen");
    }
    if (valueGenerator == null) {
      valueGenerator = values;
    } else {
      throw new IllegalStateException(
          "Value ObjectGenerator already chosen");
    }
    return this;
  }

  @Deprecated
  public CacheLoader logUsing(StatsLogger... loggers) {
    reporter.logUsing(loggers);
    return this;
  }

  public CacheLoader addLogger(StatsLogger logger) {
    reporter.addLogger(logger);
    return this;
  }

  /**
   * Will set fill the cache until the size of the cache is full.
   *
   * @return
   */
  public CacheLoader untilFilled() {
    if (terminationCondition == null) {
      terminationCondition = new FilledTerminationCondition();
    } else {
      throw new IllegalStateException(
          "TerminationCondition already chosen");
    }
    return this;
  }

  public CacheLoader iterate(long nbIterations) {
    if (terminationCondition == null) {
      terminationCondition = new IterationTerminationCondition(nbIterations);
    } else {
      throw new IllegalStateException(
          "TerminationCondition already chosen");
    }
    return this;
  }

  public CacheLoader sequentially() {
    return this.sequentially(0);
  }

  public CacheLoader sequentially(long offset) {
    if (sequenceGenerator == null) {
      sequenceGenerator = new SequentialSequenceGenerator(offset);
    } else {
      throw new IllegalStateException("SequenceGenerator already chosen");
    }
    return this;
  }

  public void run() {
    if (this.ratios.get(OPERATION.PUT) == 0.0
        && this.ratios.get(OPERATION.PUTIFABSENT) == 0.0) {
      this.ratios.put(OPERATION.GET, 1.0);
    }
    if ((this.ratios.get(OPERATION.PUT) + this.ratios.get(OPERATION.PUTIFABSENT)) > 1.0) {
      throw new RuntimeException("Sums of ratios (put and putIfAbsent) is higher than 100%");
    }
    logger.info("-- CacheAccessor operations percentage: {}", ratios.toString());

    Sequence seeds = sequenceGenerator.createSequence();
    Condition termination = terminationCondition.createCondition(caches.toArray(new CacheWrapper[caches.size()]));
    if (statistics)
      reporter.startReporting();
    long start = System.currentTimeMillis();
    do {
      long seed = seeds.next();
      for (CacheWrapper cache : caches) {
        switch (getNextOperation()) {
          case PUT:
            cache.put(keyGenerator.generate(seed), valueGenerator.generate(seed));
            break;
          case PUTIFABSENT:
            cache.putIfAbsent(keyGenerator.generate(seed), valueGenerator.generate(seed));
            break;
        }
      }
    } while (!termination.isMet());
    long stop = System.currentTimeMillis();
    if (statistics)
      reporter.stopReporting();

    logger.debug("CacheLoader put on caches took: {}ms", stop - start);
  }

  public CacheLoader enableStatistics(boolean enabled) {
    for (CacheWrapper cache : caches)
      cache.setStatisticsEnabled(enabled);
    this.statistics = enabled;
    return this;
  }

  public CacheDriver fillPartitioned(long count, int threads) {
    return this.sequentially().iterate(count / threads).partition(threads);
  }

  public Stats getFinalStats() {
	    return reporter.getFinalStats().getOverallStats();
  }

  public StatsNode getFinalStatsNode() {
    return reporter.getFinalStats();
  }

  private OPERATION getNextOperation() {
    double d = rnd.nextDouble();

    double min = 0;
    double max = this.ratios.get(OPERATION.PUTIFABSENT);
    if (d <= max)
      return OPERATION.PUTIFABSENT;

    return OPERATION.PUT;
  }
}