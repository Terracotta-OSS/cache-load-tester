package org.terracotta.ehcache.testing.driver;

import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.cache.CacheWrapperImpl;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.operation.CacheOperation;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.sequencegenerator.RandomSequenceGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.SequenceGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.SequentialSequenceGenerator;
import org.terracotta.ehcache.testing.termination.FilledTerminationCondition;
import org.terracotta.ehcache.testing.termination.TerminationCondition;
import org.terracotta.ehcache.testing.termination.TimedTerminationCondition;
import org.terracotta.ehcache.testing.validator.EqualityValidation;
import org.terracotta.ehcache.testing.validator.Validation;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.terracotta.ehcache.testing.operation.EhcacheOperation.get;

public class IndividualCacheAccessor extends CacheAccessor {
  private static Logger logger = LoggerFactory.getLogger(IndividualCacheAccessor.class);

  private final Random rnd = new Random();

  private final CacheWrapper cacheWrapper;
  private int weight = 1;

  private Set<CacheOperation> ratios = new LinkedHashSet<CacheOperation>();

  private final AtomicLong delayInMicros = new AtomicLong();

  private ObjectGenerator keyGenerator;
  private ObjectGenerator valueGenerator;

  private SequenceGenerator sequenceGenerator;

  private TerminationCondition terminationCondition;

  private Validation validation;
  private Validation.Mode validationMode;

  public IndividualCacheAccessor(Ehcache cache) {
    this.cacheWrapper = new CacheWrapperImpl(cache);
  }

  @Override
  protected void init() {
    double sumOfRatios = checkRatios();
    calculateGetRatio(sumOfRatios);

    for (CacheOperation operation : ratios) {
      operation.setCache(this.cacheWrapper);
      operation.setValidationMode(this.validationMode);
    }

    logger.info("-- CacheAccessor operations percentages: {}", ratios.toString());
  }

  /**
   * The get operation is the default one for the CacheAccessor
   * so we need to check if it was added by the user, otherwise we add it ourselves.
   * Its ratio will be 100% - the other ratios
   *
   * @param sumOfRatios the sum of ratios (all operations, get is not included yet because it was not defined)
   */
  private void calculateGetRatio(final double sumOfRatios) {
    boolean getIsDefined = false;
    for (CacheOperation ratio : ratios) {
      if (ratio.getName() == CacheOperation.OPERATIONS.GET) {
        getIsDefined = true;
        break;
      }
    }
    if (!getIsDefined) {
      final CacheOperation operation = get(1.0 - sumOfRatios);
      operation.setCache(cacheWrapper);
      ratios.add(operation);
    }
  }

  private double checkRatios() {
    double sumOfRatios = 0.0;
    for (CacheOperation ratio : ratios) {
      sumOfRatios += ratio.getRatio();
    }
    if (sumOfRatios > 1.0) {
      throw new RuntimeException("Sums of ratios is higher than 100%");
    }
    return sumOfRatios;
  }

  @Override
  public CacheAccessor andAccess(Ehcache one) {
    return new MultipleCacheAccessor(this, one);
  }

  @Override
  public CacheAccessor sequentially() {
    return this.sequentially(0);
  }

  @Override
  public CacheAccessor sequentially(long offset) {
    if (sequenceGenerator == null) {
      sequenceGenerator = new SequentialSequenceGenerator(offset);
    } else {
      throw new IllegalStateException("SequenceGenerator already chosen");
    }
    return this;
  }

  @Override
  public CacheAccessor atRandom(Distribution distribution, long min, long max, long width) {
    if (sequenceGenerator == null) {
      sequenceGenerator = new RandomSequenceGenerator(distribution, min, max, width);
    } else {
      throw new IllegalStateException("SequenceGenerator already chosen");
    }
    return this;
  }

  /**
   * executes operations, according to their weight
   * It also adds a delay, if any, before doing operation.
   *
   * @param seed
   * @param validator
   */
  public void runOnce(long seed, Validation.Validator validator) {
    try {
      TimeUnit.MICROSECONDS.sleep(delayInMicros.get());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    double d = rnd.nextDouble();
    double min, max = 0.0;

    for (CacheOperation operation : ratios) {
      min = max;
      max = min + operation.getRatio();

      if (d >= min && d < max)
        operation.exec(seed, keyGenerator, valueGenerator, validator);
    }
  }

  /**
   * Executes the test. Starts {@link org.terracotta.ehcache.testing.statistics.StatsReporter} thread and executes
   * {@link #runOnce(long, org.terracotta.ehcache.testing.validator.Validation.Validator)} till {@link TerminationCondition} is
   * met. Stops reporter thread.
   */
  @Override
  public void execute() {
    SequenceGenerator.Sequence seeds = sequenceGenerator.createSequence();
    TerminationCondition.Condition termination = terminationCondition.createCondition(cacheWrapper);

    Validation.Validator validator;
    if (validation == null) {
      validator = null;
    } else {
      validator = validation.createValidator(valueGenerator);
    }
    long start = now();
    do {
      runOnce(seeds.next(), validator);
    } while (!termination.isMet());
    long stop = now();
    logger.debug("CacheAccessor operations on caches took: {}ms", stop - start);
  }

  @Override
  public CacheAccessor stopAfter(int time, TimeUnit unit) {
    return terminateOn(new TimedTerminationCondition(time, unit));
  }

  @Override
  public CacheAccessor terminateOn(TerminationCondition termination) {
    if (terminationCondition == null) {
      terminationCondition = termination;
    } else {
      throw new IllegalStateException("TerminationCondition already chosen for cache " + this.cacheWrapper.getName());
    }
    return this;
  }

  @Override
  public CacheAccessor untilFilled() {
    return terminateOn(new FilledTerminationCondition());
  }

  @Override
  public CacheAccessor using(ObjectGenerator keys, ObjectGenerator values) {
    if (keyGenerator == null) {
      keyGenerator = keys;
    } else {
      throw new IllegalStateException("Key ObjectGenerator already chosen");
    }
    if (valueGenerator == null) {
      valueGenerator = values;
    } else {
      throw new IllegalStateException("Value ObjectGenerator already chosen");
    }
    return this;
  }

  @Override
  public CacheAccessor validate(final Validation.Mode validationMode) {
    return validateUsing(validationMode, new EqualityValidation());
  }

  @Override
  public CacheAccessor validate() {
    return validate(Validation.Mode.UPDATE);
  }

  @Override
  public CacheAccessor validateUsing(Validation validation) {
    return validateUsing(Validation.Mode.UPDATE, validation);
  }

  @Override
  public CacheAccessor validateUsing(final Validation.Mode validationMode, Validation validation) {
    if (this.validation == null) {
      this.validationMode = validationMode;
      this.validation = validation;
    } else {
      throw new IllegalStateException("Validation already chosen for cache "+ this.cacheWrapper.getName());
    }
    return this;
  }

  @Override
  public CacheAccessor withWeight(int i) {
    this.weight = i;
    return this;
  }

  @Override
  public CacheAccessor enableStatistics(boolean enabled) {
    this.statistics = enabled;
    cacheWrapper.setStatisticsEnabled(enabled);
    return this;
  }

  @Override
  public CacheAccessor accessPattern(AccessPattern.Pattern pattern, int duration, int interval) {
    throw new IllegalStateException("AccessPattern is not allowed for IndividualCacheAccessor");
  }

  @Override
  public CacheAccessor doOps(final CacheOperation... cacheOperations) {
    for (CacheOperation operation : cacheOperations) {
      ratios.add(operation);
    }
    return this;
  }

  @Override
  public CacheAccessor addThinkTime(long micros) {
    logger.debug("Delay set to : " + micros);
    this.delayInMicros.set(micros);
    return this;
  }

  public int getWeight() {
    return weight;
  }

  public CacheWrapper getCacheWrapper() {
    return cacheWrapper;
  }

  public SequenceGenerator getSequenceGenerator() {
    return sequenceGenerator;
  }

  public Validation getValidation() {
    return validation;
  }

  public ObjectGenerator getValueGenerator() {
    return valueGenerator;
  }
}
