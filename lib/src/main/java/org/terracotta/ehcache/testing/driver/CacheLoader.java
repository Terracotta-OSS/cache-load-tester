/*
 *  Copyright Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.terracotta.ehcache.testing.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.operation.CacheOperation;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

/**
 * The CacheLoader is one of the two main classes of the cache loader API. Its responsibilities are to
 * - configure the cache warmup phase
 * - execute the cache warmup phase
 *
 * @author Chris Dennis
 * @author Aurelien Broszniowski
 * @author Himadri Singh
 */
public class CacheLoader implements CacheDriver {
  private static Logger logger = LoggerFactory.getLogger(CacheLoader.class);

  private final Collection<GenericCacheWrapper> caches;

  private Set<CacheOperation> operations = new LinkedHashSet<CacheOperation>();
  private final Random rnd = new Random();

  private boolean statistics = false;
  private SequenceGenerator sequenceGenerator = null;
  private ObjectGenerator keyGenerator = null;
  private ObjectGenerator valueGenerator = null;
  private TerminationCondition terminationCondition = null;
  private final StatsReporter reporter = StatsReporter.getInstance();

  private CacheLoader(GenericCacheWrapper[] caches) {
    this.caches = new LinkedHashSet<GenericCacheWrapper>(Arrays.asList(caches));
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
  public static CacheLoader load(GenericCacheWrapper... caches) {
    return new CacheLoader(caches);
  }

  public CacheDriver partition(int count) {
    long offset = 0;
    if (sequenceGenerator != null) {
      if (sequenceGenerator instanceof SequentialSequenceGenerator)
        offset = ((SequentialSequenceGenerator)sequenceGenerator).getOffset();
    }
    Collection<CacheDriver> drivers = new ArrayList<CacheDriver>(count);
    for (int i = 0; i < count; i++) {
      drivers.add(new CacheLoader(this, new PartitionedSequentialGenerator(offset + i, count)));
    }
    return new ParallelDriver(drivers);
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
    double sumOfRatios = checkRatios();
    calculateDefaultOpRatio(sumOfRatios);

    logger.info("-- CacheLoader loader percentage: {}", operations.toString());

    Sequence seeds = sequenceGenerator.createSequence();
    Condition termination = terminationCondition.createCondition(caches.toArray(new GenericCacheWrapper[caches.size()]));
    if (statistics)
      reporter.startReporting();
    long start = System.currentTimeMillis();
    do {
      long seed = seeds.next();
      for (GenericCacheWrapper cache : caches) {
        runOnce(seed, cache);
      }
    } while (!termination.isMet());
    long stop = System.currentTimeMillis();
    if (statistics)
      reporter.stopReporting();

    logger.debug("CacheLoader put on caches took: {}ms", stop - start);
  }

  /**
   * executes operations, according to their weight
   * It also adds a delay, if any, before doing operation.
   *
   * @param seed
   */
  public void runOnce(long seed, GenericCacheWrapper cacheWrapper) {
    double d = rnd.nextDouble();
    double min, max = 0.0;

    for (CacheOperation operation : operations) {
      min = max;
      max = min + operation.getRatio();

      //TODO : doesnt need to pass the seed since it's already in cachewrapper's generator, why pass validator?
      if (d >= min && d < max)
        operation.exec(cacheWrapper, seed, keyGenerator, valueGenerator, null);
    }
  }

  /**
   * Calculate the default operation ratio for the CacheLoader
   * so we need to check if it was added by the user, otherwise we add it ourselves.
   * Its ratio will be (100% - the other ratios)
   *
   * @param sumOfRatios the sum of ratios (all operations, default is not included yet because it was not defined)
   */
  private void calculateDefaultOpRatio(final double sumOfRatios) {
    boolean defaultOpIsDefined = false;

    GenericCacheWrapper cacheWrapper = caches.iterator().hasNext() ? caches.iterator().next() : null;
    String defaultLoaderOperationName = cacheWrapper!= null ? cacheWrapper.getDefaultLoaderOperationName() : "";

    for (CacheOperation operation : operations) {
      if (operation.getName().equals(defaultLoaderOperationName)) {
        defaultOpIsDefined = true;
        break;
      }
    }
    if (!defaultOpIsDefined && cacheWrapper != null) {
      final CacheOperation operation = cacheWrapper.getDefaultLoaderOperation(1.0 - sumOfRatios);
      operations.add(operation);
    }
  }

  private double checkRatios() {
    double sumOfRatios = 0.0;
    for (CacheOperation ratio : operations) {
      sumOfRatios += ratio.getRatio();
    }
    if (sumOfRatios > 1.0) {
      throw new RuntimeException("Sums of ratios is higher than 100%");
    }
    return sumOfRatios;
  }

  public CacheLoader enableStatistics(boolean enabled) {
    for (GenericCacheWrapper cache : caches)
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

  public CacheLoader doOps(final CacheOperation... cacheOperations) {
    Collections.addAll(operations, cacheOperations);
    return this;
  }

}