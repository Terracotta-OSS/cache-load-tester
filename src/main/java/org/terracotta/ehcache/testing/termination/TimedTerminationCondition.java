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
package org.terracotta.ehcache.testing.termination;

import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;

import java.util.concurrent.TimeUnit;


public class TimedTerminationCondition implements TerminationCondition {

  private final long time;

  public TimedTerminationCondition(int time, TimeUnit unit) {
    this.time = unit.toNanos(time);
  }

  @Override
  public Condition createCondition(GenericCacheWrapper... caches) {
    return new TimedCondition(time);
  }

  static class TimedCondition implements Condition {

    private final long end;
    private final boolean eternal;

    public TimedCondition(long time) {
      this.eternal = (time < 0);
      this.end = System.nanoTime() + time;
    }

    @Override
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
