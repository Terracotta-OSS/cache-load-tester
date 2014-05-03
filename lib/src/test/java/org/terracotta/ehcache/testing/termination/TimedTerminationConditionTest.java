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

import org.junit.Assert;
import org.junit.Test;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

/**
 * @author Aurelien Broszniowski
 */

public class TimedTerminationConditionTest {

  @Test
  public void testFailingTimedCondition() throws Exception {
    TimedTerminationCondition termination = new TimedTerminationCondition(4, TimeUnit.SECONDS);
    GenericCacheWrapper cache = mock(GenericCacheWrapper.class);
    final TerminationCondition.Condition condition = termination.createCondition(cache);
    try {
      await().atMost(2 + 1, TimeUnit.SECONDS).until(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return (condition.isMet() == true);
        }
      });
      Assert.fail("condition should not have met within the time");
    } catch (TimeoutException e) {

    }
  }

  @Test
  public void testSuccessfulTimedCondition() throws Exception {
    TimedTerminationCondition termination = new TimedTerminationCondition(2, TimeUnit.SECONDS);
    GenericCacheWrapper cache = mock(GenericCacheWrapper.class);
    final TerminationCondition.Condition condition = termination.createCondition(cache);
    await().atMost(5 + 1, TimeUnit.SECONDS).until(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return (condition.isMet() == true);
      }
    });
  }
}
