package org.terracotta.ehcache.testing.termination;

import org.terracotta.ehcache.testing.cache.CacheWrapper;

public class IterationTerminationCondition implements TerminationCondition {

  protected final int nbIterations;

  public IterationTerminationCondition(final int nbIterations) {
    this.nbIterations = nbIterations;
  }

  public Condition createCondition(final CacheWrapper ... caches) {
    return new IteratedCondition(caches);
  }

  class IteratedCondition implements Condition {

    private int counter = 0;

    public IteratedCondition(final CacheWrapper[] caches) {
      // no-op
    }

    public boolean isMet() {
      return ++counter > nbIterations - 1;
    }
  }
}