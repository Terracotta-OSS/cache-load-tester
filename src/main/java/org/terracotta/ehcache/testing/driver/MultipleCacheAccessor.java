package org.terracotta.ehcache.testing.driver;

import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.operation.CacheOperation;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.sequencegenerator.SequenceGenerator;
import org.terracotta.ehcache.testing.termination.FilledTerminationCondition;
import org.terracotta.ehcache.testing.termination.TerminationCondition;
import org.terracotta.ehcache.testing.termination.TimedTerminationCondition;
import org.terracotta.ehcache.testing.validator.Validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MultipleCacheAccessor extends CacheAccessor {
  private static Logger logger = LoggerFactory.getLogger(MultipleCacheAccessor.class);

  private final List<IndividualCacheAccessor> accessors = new ArrayList<IndividualCacheAccessor>();
  private AccessPattern accessPattern = null;
  private TerminationCondition terminationCondition;
  private static Thread access = null;

  public MultipleCacheAccessor(IndividualCacheAccessor one, Ehcache two) {
    accessors.add(one);
    accessors.add(new IndividualCacheAccessor(two));
  }

  @Override
  protected void init() {
    for (IndividualCacheAccessor accessor : accessors)
      accessor.init();
  }

  @Override
  public CacheAccessor andAccess(Ehcache cache) {
    accessors.add(new IndividualCacheAccessor(cache));
    return this;
  }

  @Override
  public CacheAccessor sequentially() {
    return this.sequentially(0);
  }

  @Override
  public CacheAccessor sequentially(final long offset) {
    for (Iterator<IndividualCacheAccessor> it = accessors.iterator(); it.hasNext(); ) {
      try {
        it.next().sequentially(offset);
      } catch (IllegalStateException e) {
        if (!it.hasNext()) {
          throw e;
        }
      }
    }
    return this;
  }

  @Override
  public CacheAccessor atRandom(Distribution distribution, long min, long max, long width) {
    for (Iterator<IndividualCacheAccessor> it = accessors.iterator(); it.hasNext(); ) {
      try {
        it.next().atRandom(distribution, min, max, width);
      } catch (IllegalStateException e) {
        if (!it.hasNext()) {
          throw e;
        }
      }
    }
    return this;
  }

  private IndividualCacheAccessor latestAccessor() {
    return accessors.get(accessors.size() - 1);
  }

  @Override
  public void execute() {
    if (accessPattern != null)
      accessWithPattern();
    else
      accessWithWeight();
  }

  private void accessWithPattern() {
    synchronized (MultipleCacheAccessor.class) {
      if (this.accessPattern != null && access == null) {
        access = new Thread(accessPattern);
        access.start();
      }
    }
    for (IndividualCacheAccessor a : accessors)
      a.terminateOn(terminationCondition);

    ParallelDriver driver = new ParallelDriver(accessors);
    driver.run();
    if (this.accessPattern != null)
      access.interrupt();
  }

  private void accessWithWeight() {
    Collection<CacheWrapper> caches = new ArrayList<CacheWrapper>();
    Map<Integer, IndividualCacheAccessor> selection = new HashMap<Integer, IndividualCacheAccessor>();
    Map<IndividualCacheAccessor, SequenceGenerator.Sequence> sequences = new IdentityHashMap<IndividualCacheAccessor, SequenceGenerator.Sequence>();
    Map<IndividualCacheAccessor, Validation.Validator> validators = new IdentityHashMap<IndividualCacheAccessor, Validation.Validator>();

    int totalWeight = 0;
    for (IndividualCacheAccessor a : accessors) {
      int weight = a.getWeight();
      if (weight <= 0) {
        continue;
      } else {
        selection.put(totalWeight, a);
        caches.add(a.getCacheWrapper());
        sequences.put(a, a.getSequenceGenerator().createSequence());
        if (a.getValidation() != null) {
          validators.put(a, a.getValidation().createValidator(a.getValueGenerator()));
        }
        totalWeight += a.getWeight();
      }
    }

    Random rnd = new Random();
    TerminationCondition.Condition termination = terminationCondition.createCondition(caches.toArray(
        new CacheWrapper[caches.size()]));

    long start = now();
    do {
      int selector = rnd.nextInt(totalWeight);
      IndividualCacheAccessor accessor;
      while ((accessor = selection.get(selector--)) == null) ;
      accessor.runOnce(sequences.get(accessor).next(), validators.get(accessor));
    } while (!termination.isMet());
    long stop = now();
    logger.debug("CacheAccessor put/get/validate on caches took: {}ms", stop - start);
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
      throw new IllegalStateException("TerminationCondition already chosen");
    }
    return this;
  }

  @Override
  public CacheAccessor untilFilled() {
    return terminateOn(new FilledTerminationCondition());
  }

  @Override
  public CacheAccessor using(ObjectGenerator keys, ObjectGenerator values) {
    for (Iterator<IndividualCacheAccessor> it = accessors.iterator(); it.hasNext(); ) {
      try {
        it.next().using(keys, values);
      } catch (IllegalStateException e) {
        if (!it.hasNext()) {
          throw e;
        }
      }
    }
    return this;
  }

  @Override
  public CacheAccessor validateUsing(Validation validation) {
    return validateUsing(Validation.Mode.UPDATE, validation);
  }

  @Override
  public CacheAccessor validate(final Validation.Mode validationMode) {
    for (Iterator<IndividualCacheAccessor> it = accessors.iterator(); it.hasNext(); ) {
      try {
        it.next().validate(validationMode);
      } catch (IllegalStateException e) {
        if (!it.hasNext()) {
          throw e;
        }
      }
    }
    return this;
  }

  @Override
  public CacheAccessor validate() {
    return validate(Validation.Mode.UPDATE);
  }

  @Override
  public CacheAccessor validateUsing(final Validation.Mode validationMode, Validation validation) {
    for (Iterator<IndividualCacheAccessor> it = accessors.iterator(); it.hasNext(); ) {
      try {
        it.next().validateUsing(validationMode, validation);
      } catch (IllegalStateException e) {
        if (!it.hasNext()) {
          throw e;
        }
      }
    }
    return this;
  }

  @Override
  public CacheAccessor withWeight(int i) {
    latestAccessor().withWeight(i);
    return this;
  }

  @Override
  public CacheAccessor enableStatistics(boolean statistics) {
    this.statistics = statistics;
    for (IndividualCacheAccessor accessor : accessors)
      accessor.enableStatistics(statistics);
    return this;
  }

  @Override
  public CacheAccessor accessPattern(AccessPattern.Pattern pattern, int duration, int interval) {
    accessPattern = AccessPattern.create(pattern).setDuration(duration).setInterval(interval).setAccessors(accessors);
    return this;
  }

  @Override
  public CacheAccessor doOps(final CacheOperation... cacheOperations) {
    for (IndividualCacheAccessor individualCacheAccessor : accessors)
      individualCacheAccessor.doOps(cacheOperations);
    return this;
  }

  @Override
  public CacheAccessor addThinkTime(long micros) {
    for (IndividualCacheAccessor accessor : accessors)
      accessor.addThinkTime(micros);
    return this;
  }
}

