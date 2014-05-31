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
package org.jsoftbiz;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.operation.CacheOperation;
import org.terracotta.ehcache.testing.validator.Validation;

import java.util.concurrent.TimeoutException;

/**
 * Defines a number of Memcached operations to be used for testing
 *
 * @author Aurelien Broszniowski
 */


public class MemcachedOperation {

  public enum OPERATIONS {
    SET, GET, DELETE
  }

  public static CacheOperation get(final double ratio) {
    return get(ratio, MemcachedClient.DEFAULT_OP_TIMEOUT);
  }

  public static CacheOperation get(final double ratio, final long timeout) {

    return new CacheOperation(ratio) {

      @Override
      public Object exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        Object value = null;
        String key = keyGenerator.generate(seed).toString();
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          value = ((MemcachedClient)cache.getCache()).get("" + seed, timeout);
        } catch (TimeoutException e) {
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (InterruptedException e) {
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (MemcachedException e) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getReadStats().add(end - start);
        }

        if (Validation.Mode.STRICT.equals(getValidationMode())) {
          if (validator == null) {                          // TODO optimize : move check at startup
            throw new AssertionError("Validator is null");
          }
          validator.validate(seed, value);
        } else {
          if (value == null) {
            try {
              ((MemcachedClient)cache.getCache()).set(key, 0, valueGenerator.generate(seed));
            } catch (TimeoutException e) {
              cache.getWriteStats().incrementTotalExceptionCount();
            } catch (InterruptedException e) {
              cache.getWriteStats().incrementTotalExceptionCount();
            } catch (MemcachedException e) {
              cache.getWriteStats().incrementTotalExceptionCount();
            }
          } else if (validator != null) {
            validator.validate(seed, value);
          }
        }
        return value;
      }

      @Override
      public String getName() {
        return OPERATIONS.GET.name();
      }
    };
  }

  public static CacheOperation delete(final double ratio) {

    return new CacheOperation(ratio) {

      @Override
      public Boolean exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        boolean removed = false;
        String key = keyGenerator.generate(seed).toString();
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          removed = ((MemcachedClient)cache.getCache()).delete(key);
        } catch (InterruptedException e) {
          cache.getRemoveStats().incrementTotalExceptionCount();
        } catch (TimeoutException e) {
          cache.getRemoveStats().incrementTotalExceptionCount();
        } catch (MemcachedException e) {
          cache.getRemoveStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getRemoveStats().add(end - start);
        }
        return removed;
      }

      @Override
      public String getName() {
        return OPERATIONS.DELETE.name();
      }
    };
  }

  public static CacheOperation set(final double ratio) {
    return set(ratio, 0);
  }

  /**
   * Store key-value item to memcached
   *
   * @param ratio
   * @param expiration An expiration time, in seconds. Can be up to 30 days. After 30
   *                   days, is treated as a unix timestamp of an exact date.
   * @return
   */
  public static CacheOperation set(final double ratio, final int expiration) {

    return new CacheOperation(ratio) {

      @Override
      public Void exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        String key = "" + keyGenerator.generate(seed);
        Object value = valueGenerator.generate(seed);
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          ((MemcachedClient)cache.getCache()).set(key, expiration, value);
        } catch (InterruptedException e) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (TimeoutException e) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (MemcachedException e) {
          cache.getWriteStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return null;
      }

      @Override
      public String getName() {
        return OPERATIONS.SET.name();
      }
    };
  }
}
