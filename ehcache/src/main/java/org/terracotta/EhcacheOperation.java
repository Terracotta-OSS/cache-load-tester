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
package org.terracotta;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;
import net.sf.ehcache.constructs.nonstop.RejoinCacheException;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.operation.CacheOperation;
import org.terracotta.ehcache.testing.validator.Validation;

/**
 * Defines a number of Ehcache operations to be used for testing
 *
 * @author Aurelien Broszniowski
 */

public class EhcacheOperation {

  public enum OPERATIONS {
    GET, UPDATE, REMOVE, REMOVE_ELEMENT, REPLACE, REPLACE_ELEMENT, PUT, PUT_IF_ABSENT, PUT_WITH_WRITER, PUT_CONTROLLED_THROUGHPUT;
  }

  public static CacheOperation get(final double ratio) {
    return new CacheOperation(ratio) {

      @Override
      public Object exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        Element value = null;
        Object key = keyGenerator.generate(seed);
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          value = ((Ehcache)cache.getCache()).get(key);
        } catch (NonStopCacheException nsce) {
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
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
          if (value == null) {
            validator.validate(seed, null);
          } else {
            validator.validate(seed, value.getObjectValue());
          }
        } else {
          if (value == null) {
            ((Ehcache)cache.getCache()).put(new Element(key, valueGenerator.generate(seed)));
          } else if (validator != null) {
            validator.validate(seed, value.getObjectValue());
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

  public static CacheOperation remove(final double ratio) {
    return new CacheOperation(ratio) {

      @Override
      public Boolean exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        boolean removed = false;
        Object key = keyGenerator.generate(seed);
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          removed = ((Ehcache)cache.getCache()).remove(key);
        } catch (NonStopCacheException nsce) {
          cache.getRemoveStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
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
        return OPERATIONS.REMOVE.name();
      }
    };
  }

  public static CacheOperation removeElement(final double ratio) {
    return new CacheOperation(ratio) {

      @Override
      public Boolean exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        boolean removed = false;
        Element elementToRemove = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          removed = ((Ehcache)cache.getCache()).removeElement(elementToRemove);
        } catch (NonStopCacheException nsce) {
          cache.getRemoveStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
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
        return OPERATIONS.REMOVE_ELEMENT.name();
      }
    };
  }

  public static CacheOperation putIfAbsent(final double ratio) {

    return new CacheOperation(ratio) {

      @Override
      public Object exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        Element element = null;
        Element elementToPut = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          element = ((Ehcache)cache.getCache()).putIfAbsent(elementToPut);
        } catch (NonStopCacheException nsce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return element;
      }

      @Override
      public String getName() {
        return OPERATIONS.PUT_IF_ABSENT.name();
      }
    };
  }

  public static CacheOperation put(final double ratio) {

    return new CacheOperation(ratio) {

      @Override
      public Void exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        Element elementToPut = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          ((Ehcache)cache.getCache()).put(elementToPut);
        } catch (NonStopCacheException nsce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
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
        return OPERATIONS.PUT.name();
      }
    };
  }

  public static CacheOperation putWithWriter(final double ratio) {

    return new CacheOperation(ratio) {

      @Override
      public Void exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        Element elementToPut = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          ((Ehcache)cache.getCache()).putWithWriter(elementToPut);
        } catch (NonStopCacheException nsce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
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
        return OPERATIONS.PUT_WITH_WRITER.name();
      }
    };
  }

  public static CacheOperation replaceElement(final double ratio) {

    return new CacheOperation(ratio) {

      @Override
      public Boolean exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        boolean replaced = false;
        Element oldElementToTestAgainst = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        Element elementToPut = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          replaced = ((Ehcache)cache.getCache()).replace(oldElementToTestAgainst, elementToPut);
        } catch (NonStopCacheException nsce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return replaced;
      }

      @Override
      public String getName() {
        return OPERATIONS.REPLACE_ELEMENT.name();
      }
    };
  }

  public static CacheOperation replace(final double ratio) {

    return new CacheOperation(ratio) {

      @Override
      public Element exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        Element oldReplacedElement = null;
        Element elementToPut = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          oldReplacedElement = ((Ehcache)cache.getCache()).replace(elementToPut);
        } catch (NonStopCacheException nsce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return oldReplacedElement;
      }

      @Override
      public String getName() {
        return OPERATIONS.REPLACE.name();
      }
    };
  }

  public static CacheOperation update(final double ratio) {

    return new CacheOperation(ratio) {

      @Override
      public Void exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        Element elementToPut = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          ((Ehcache)cache.getCache()).put(elementToPut);
        } catch (NonStopCacheException nsce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
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
        return OPERATIONS.UPDATE.name();
      }
    };
  }

  /**
   * Put Elements in the cache with a limit on the max TPS
   *
   * @param ratio % of put operations
   * @return null
   */
  public static CacheOperation putWithControlledThroughput(final double ratio) {

    final int tpsThreshold = Integer.getInteger("tpsThreshold", -1);

    return new CacheOperation(ratio) {
      @Override
      public Object exec(final GenericCacheWrapper cache, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        if (tpsThreshold != -1) {
          while (cache.getWriteStats().getThroughput() > tpsThreshold) {
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
        Element elementToPut = new Element(keyGenerator.generate(seed), valueGenerator.generate(seed));
        long start = (cache.isStatisticsEnabled()) ? now() : 0;
        try {
          ((Ehcache)cache.getCache()).put(elementToPut);
        } catch (RejoinCacheException rce) {
          cache.getWriteStats().incrementTotalExceptionCount();
        } catch (NonStopCacheException nsce) {
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
        return OPERATIONS.PUT_CONTROLLED_THROUGHPUT.name();
      }
    };
  }
}
