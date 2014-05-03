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

import java.util.concurrent.atomic.AtomicLong;

public class IterationTerminationCondition implements TerminationCondition {

  protected final long nbIterations;

  public IterationTerminationCondition(final long nbIterations) {
    this.nbIterations = nbIterations;
  }

  @Override
  public Condition createCondition(final GenericCacheWrapper... caches) {
    return new IteratedCondition(caches);
  }

  class IteratedCondition implements Condition {

    private final AtomicLong counter;

    public IteratedCondition(final GenericCacheWrapper[] caches) {
      counter = new AtomicLong();
    }

    @Override
    public boolean isMet() {
      return counter.incrementAndGet() > nbIterations - 1;
    }
  }
}