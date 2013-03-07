package org.terracotta.ehcache.testing.termination;

import java.util.concurrent.TimeUnit;

import org.terracotta.ehcache.testing.cache.CacheWrapper;


public class TimedTerminationCondition implements TerminationCondition {

  private final long time;

  public TimedTerminationCondition(int time, TimeUnit unit) {
    this.time = unit.toNanos(time);
  }

  public Condition createCondition(CacheWrapper ... caches) {
    return new TimedCondition(time);
  }

  static class TimedCondition implements Condition {

    private final long end;
    private final boolean eternal;

    public TimedCondition(long time) {
      this.eternal = (time < 0);
      this.end = System.nanoTime() + time;
    }

    public boolean isMet() {
   	  if (eternal)
    	return false;
      if (System.nanoTime() > end) {
        return true;
      } else {
        return false;
      }
    }

  }

}
