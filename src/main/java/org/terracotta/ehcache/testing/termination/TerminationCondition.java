package org.terracotta.ehcache.testing.termination;

import org.terracotta.ehcache.testing.cache.CacheWrapper;

public interface TerminationCondition {

  Condition createCondition(final CacheWrapper  ... caches);

  public interface Condition {

    boolean isMet();
  }
}
