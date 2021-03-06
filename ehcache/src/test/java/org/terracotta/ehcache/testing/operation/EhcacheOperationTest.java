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
package org.terracotta.ehcache.testing.operation;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.Test;
import org.mockito.Matchers;
import org.terracotta.EhcacheOperation;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.validator.Validation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Aurelien Broszniowski
 */

public class EhcacheOperationTest {

  @Test
  public void testPutWithControlledThroughput() {
    System.setProperty("tpsThreshold", "500");
    CacheOperation operation = EhcacheOperation.putWithControlledThroughput(1.0);
    assertThat(operation.getName(), is(equalTo(EhcacheOperation.OPERATIONS.PUT_CONTROLLED_THROUGHPUT.name())));
    GenericCacheWrapper cacheWrapper = mock(GenericCacheWrapper.class);
    ObjectGenerator keyGenerator = mock(ObjectGenerator.class);
    ObjectGenerator valueGenerator = mock(ObjectGenerator.class);
    Validation.Validator validator = mock(Validation.Validator.class);
    Stats stats = mock(Stats.class);
    Ehcache ehcache = mock(Ehcache.class);

    when(keyGenerator.generate(1L)).thenReturn("key");
    when(valueGenerator.generate(1L)).thenReturn("value");
    when(cacheWrapper.getWriteStats()).thenReturn(stats);
    when(stats.getThroughput()).thenReturn(800L, 700L, 600L, 500L, 400L, 400L, 450L, 470L, 490L, 500L);
    when((Ehcache)cacheWrapper.getCache()).thenReturn(ehcache);

    for (int i = 0; i < 5; i++) {
      operation.exec(cacheWrapper, 1L, keyGenerator, valueGenerator, validator);
    }

    verify(keyGenerator, times(5)).generate(1L);
    verify(valueGenerator, times(5)).generate(1L);
    verify(validator, times(0)).validate(anyLong(), anyObject());
    verify((ehcache), times(5)).put((Element)Matchers.any());

  }

}
