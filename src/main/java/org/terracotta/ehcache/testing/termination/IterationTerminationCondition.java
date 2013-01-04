package org.terracotta.ehcache.testing.termination;

import java.util.concurrent.atomic.AtomicLong;

import org.terracotta.ehcache.testing.cache.CacheWrapper;

public class IterationTerminationCondition implements TerminationCondition {

  protected final long nbIterations;

  public IterationTerminationCondition(final long nbIterations) {
    this.nbIterations = nbIterations;
  }

  public Condition createCondition(final CacheWrapper ... caches) {
    return new IteratedCondition(caches);
  }

  class IteratedCondition implements Condition {

    private final AtomicLong counter;

    public IteratedCondition(final CacheWrapper[] caches) {
      counter = new AtomicLong();
    }

    public boolean isMet() {
      return counter.incrementAndGet() > nbIterations - 1;
    }
  }
}