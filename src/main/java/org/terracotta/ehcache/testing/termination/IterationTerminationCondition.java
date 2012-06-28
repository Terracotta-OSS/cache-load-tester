package org.terracotta.ehcache.testing.termination;

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

    private long counter = 0;

    public IteratedCondition(final CacheWrapper[] caches) {
      // no-op
    }

    public boolean isMet() {
      return ++counter > nbIterations - 1;
    }
  }
}