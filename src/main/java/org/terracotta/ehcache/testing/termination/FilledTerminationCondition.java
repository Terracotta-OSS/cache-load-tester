package org.terracotta.ehcache.testing.termination;

import java.util.IdentityHashMap;
import java.util.Map;

import org.terracotta.ehcache.testing.cache.CacheWrapper;

public class FilledTerminationCondition implements TerminationCondition {

  public Condition createCondition(CacheWrapper ... caches) {
    return new FilledCondition(caches);
  }

  static class FilledCondition implements Condition {
    private final Map<CacheWrapper, Integer> sizes = new IdentityHashMap<CacheWrapper, Integer>();

    public FilledCondition(CacheWrapper[] caches) {
      for (CacheWrapper cache : caches) {
        sizes.put(cache, cache.getSize());
      }
    }

    public synchronized boolean isMet() {
      boolean isMet = true;

      for (Map.Entry<CacheWrapper, Integer> e : sizes.entrySet()) {
        int current = e.getKey().getSize();
        if (current > e.getValue().intValue()) {
           isMet &= false;
           e.setValue(current);
        }
      }
      return isMet;
    }
  }

}
