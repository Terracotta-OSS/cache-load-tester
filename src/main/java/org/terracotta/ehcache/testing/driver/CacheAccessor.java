package org.terracotta.ehcache.testing.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.cache.CacheWrapperImpl;
import org.terracotta.ehcache.testing.driver.AccessPattern.Pattern;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.Distribution;
import org.terracotta.ehcache.testing.sequencegenerator.RandomSequenceGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.SequenceGenerator;
import org.terracotta.ehcache.testing.sequencegenerator.SequenceGenerator.Sequence;
import org.terracotta.ehcache.testing.sequencegenerator.SequentialSequenceGenerator;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsNode;
import org.terracotta.ehcache.testing.statistics.StatsReporter;
import org.terracotta.ehcache.testing.statistics.logger.StatsLogger;
import org.terracotta.ehcache.testing.termination.FilledTerminationCondition;
import org.terracotta.ehcache.testing.termination.TerminationCondition;
import org.terracotta.ehcache.testing.termination.TerminationCondition.Condition;
import org.terracotta.ehcache.testing.termination.TimedTerminationCondition;
import org.terracotta.ehcache.testing.validator.EqualityValidation;
import org.terracotta.ehcache.testing.validator.Validation;
import org.terracotta.ehcache.testing.validator.Validation.Validator;


public abstract class CacheAccessor implements CacheDriver {
  private static Logger logger = LoggerFactory.getLogger(CacheAccessor.class);
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

  public static CacheAccessor access(Ehcache one) {
    return new IndividualCacheAccessor(one);
  }

  public abstract CacheAccessor andAccess(Ehcache one);

  public abstract CacheAccessor sequentially();

  public abstract CacheAccessor sequentially(long offset);

  /**
   * Sets weight for the current {@link IndividualCacheAccessor}<br>
   * Will be ignored if {@link #accessPattern(Pattern, int, int)} is set
   *
   * @param i
   * @return
   */
  public abstract CacheAccessor withWeight(int i);

  public abstract CacheAccessor using(ObjectGenerator integers, ObjectGenerator fixedSize);

  public abstract CacheAccessor atRandom(Distribution normal, long min, long max, long width);

  /**
   * stop the test after specified time
   * @param time
   * @param unit
   * @return
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
   * @param termination
   * @return
   */
  public abstract CacheAccessor terminateOn(TerminationCondition termination);

  public abstract CacheAccessor validate();

  public abstract CacheAccessor validateUsing(Validation validator);

  /**
   * Enable statistics collection
   *
   * @param statistics
   * @return this
   */
  public abstract CacheAccessor enableStatistics(boolean statistics);

  /**
   * Sets access pattern for the {@link MultipleCacheAccessor}
   * @param pattern {@link Pattern} type
   * @param duration durationInSecs for transition (SPIKE,WAVE)
   * @param interval interval in secs
   * @return this
   */
  public abstract CacheAccessor accessPattern(Pattern pattern, int duration, int interval);

  /**
   * Sets update ratio.
   *
   * 0.0 = Readonly
   * 1.0 = 100% Updates
   *
   * @param updateRatio value between 0.0 - 1.0
   * @return
   */
  public abstract CacheAccessor updateRatio(double updateRatio);

  protected abstract void execute();

  public void run() {
    startReporting();
    execute();
    stopReporting();
  }

  /**
   * Use addLogger instead
   * @param loggers
   * @return CacheAccessor
   */
  public CacheAccessor logUsing(StatsLogger... loggers) {
    reporter.logUsing(loggers);
    return this;
  }

  public CacheAccessor addLogger(StatsLogger logger) {
    reporter.addLogger(logger);
    return this;
  }

  public Stats getFinalStats(){
	  return reporter.getFinalStats().getOverallStats();
  }

  public StatsNode getFinalStatsNode(){
	  return reporter.getFinalStats();
  }

  private static long now(){
  	return System.currentTimeMillis();
  }

  protected void startReporting(){
      if (statistics)
    	  reporter.startReporting();
  }

  protected void stopReporting(){
      if (statistics)
    	  reporter.stopReporting();
  }

  static class IndividualCacheAccessor extends CacheAccessor {

	private final Random rnd = new Random();

	private final CacheWrapper cacheWrapper;
    private int weight = 1;
    private double updateRatio = 0.0;
    private final AtomicLong delayInMicros = new AtomicLong();

    private ObjectGenerator keyGenerator;
    private ObjectGenerator valueGenerator;

    private SequenceGenerator sequenceGenerator;

    private TerminationCondition terminationCondition;

    private Validation validation;

    public IndividualCacheAccessor(Ehcache cache) {
      this.cacheWrapper = new CacheWrapperImpl(cache);
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
     * Do a get operation and validate the output.
     * If gets a null, inserts a new object in place.
     *
     * @param seed
     * @param validator
     */
    private void getOnce(long seed, Validator validator) {
      Object key = keyGenerator.generate(seed);
      Object value = cacheWrapper.get(key);
      if (value == null) {
        cacheWrapper.put(key, valueGenerator.generate(seed));
      } else if (validator != null) {
        validator.validate(seed, value);
      }
    }

    private void updateOnce(long seed) {
        Object key = keyGenerator.generate(seed);
    	cacheWrapper.put(key, valueGenerator.generate(seed));
    }

    /**
     * If true, do an update operation
     * If false, do a read operation
     *
     * @return boolean
     */
    private boolean shouldUpdate(){
	  return (rnd.nextDouble() < updateRatio);
    }

    /**
     * executes read/write operation depending on {@link #shouldUpdate()}.
     * It also adds a delay, if any, before doing operation.
     * @param seed
     * @param validator
     */
    public void runOnce(long seed, Validator validator){
		try {
		  TimeUnit.MICROSECONDS.sleep(delayInMicros.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
   	  if (shouldUpdate())
    	updateOnce(seed);
      else
    	getOnce(seed, validator);
    }

	/**
	 * Executes the test. Starts {@link StatsReporter} thread and executes
	 * {@link #runOnce(int, Validator)} till {@link TerminationCondition} is
	 * met. Stops reporter thread.
	 */
	@Override
	public void execute() {
      Sequence seeds = sequenceGenerator.createSequence();
      Condition termination = terminationCondition.createCondition(cacheWrapper);

      Validator validator;
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
    	logger.warn("TerminationCondition already chosen " + this.cacheWrapper.getName());
//        throw new IllegalStateException("TerminationCondition already chosen");
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
    public CacheAccessor validate() {
      return validateUsing(new EqualityValidation());
    }

    @Override
    public CacheAccessor validateUsing(Validation validation) {
      if (this.validation == null) {
        this.validation = validation;
      } else {
        throw new IllegalStateException("Validation already chosen");
      }
      return this;
    }

    @Override
    public CacheAccessor withWeight(int i) {
      this.weight = i;
      return this;
    }

	@Override
	public CacheAccessor updateRatio(double updateRatio) {
		this.updateRatio = updateRatio;
		return this;
	}

	@Override
	public CacheAccessor enableStatistics(boolean enabled) {
		this.statistics = enabled;
		cacheWrapper.setStatisticsEnabled(enabled);
		return this;
	}

	@Override
	public CacheAccessor accessPattern(Pattern pattern, int duration,
			int interval) {
		throw new IllegalStateException("AccessPattern is not allowed for IndividualCacheAccessor");
	}

	void introduceDelay(long millis) {
		logger.debug("Delay set to : " + millis);
		this.delayInMicros.set(millis);
	}

}

  static class MultipleCacheAccessor extends CacheAccessor {

    private final List<IndividualCacheAccessor> accessors = new ArrayList<IndividualCacheAccessor>();
    private AccessPattern accessPattern = null;
    private TerminationCondition terminationCondition;
    private static Thread access = null;

    public MultipleCacheAccessor(IndividualCacheAccessor one, Ehcache two) {
      accessors.add(one);
      accessors.add(new IndividualCacheAccessor(two));
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

	private void accessWithPattern(){
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


	private void accessWithWeight(){
	     Collection<CacheWrapper> caches = new ArrayList<CacheWrapper>();
	      Map<Integer, IndividualCacheAccessor> selection = new HashMap<Integer, IndividualCacheAccessor>();
	      Map<IndividualCacheAccessor, Sequence> sequences = new IdentityHashMap<IndividualCacheAccessor, Sequence>();
	      Map<IndividualCacheAccessor, Validator> validators = new IdentityHashMap<IndividualCacheAccessor, Validator>();

	      int totalWeight = 0;
	      for (IndividualCacheAccessor a : accessors) {
	        int weight = a.weight;
	        if (weight <= 0) {
	          continue;
	        } else {
	          selection.put(totalWeight, a);
	          caches.add(a.cacheWrapper);
	          sequences.put(a, a.sequenceGenerator.createSequence());
	          if (a.validation != null) {
	            validators.put(a, a.validation.createValidator(a.valueGenerator));
	          }
	          totalWeight += a.weight;
	        }
	      }

	      Random rndm = new Random();
		  Condition termination = terminationCondition.createCondition(caches
				.toArray(new CacheWrapper[caches.size()]));

	      long start = now();
	      do {
	        int selector = rndm.nextInt(totalWeight);
	        IndividualCacheAccessor accessor;
	        while ((accessor = selection.get(selector--)) == null);
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
    public CacheAccessor validate() {
      for (Iterator<IndividualCacheAccessor> it = accessors.iterator(); it.hasNext(); ) {
        try {
          it.next().validate();
        } catch (IllegalStateException e) {
          if (!it.hasNext()) {
            throw e;
          }
        }
      }
      return this;
    }

    @Override
    public CacheAccessor validateUsing(Validation validator) {
      for (Iterator<IndividualCacheAccessor> it = accessors.iterator(); it.hasNext(); ) {
        try {
          it.next().validateUsing(validator);
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
	public CacheAccessor updateRatio(double updateRatio) {
	  for (IndividualCacheAccessor individualCacheAccessor : accessors)
		individualCacheAccessor.updateRatio(updateRatio);
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
	public CacheAccessor accessPattern(Pattern pattern, int duration,
			int interval) {
		accessPattern = AccessPattern.create(pattern).setDuration(
					duration).setInterval(interval).setAccessors(accessors);
		return this;
	}

  }

 }
