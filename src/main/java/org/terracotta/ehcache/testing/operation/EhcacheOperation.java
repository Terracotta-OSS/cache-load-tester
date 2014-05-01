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
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;
import net.sf.ehcache.constructs.nonstop.RejoinCacheException;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.validator.Validation;

public class EhcacheOperation {

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
      public OPERATIONS getName() {
        return OPERATIONS.GET;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getRemoveStats().add(end - start);
        }
        return removed;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.REMOVE;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getRemoveStats().add(end - start);
        }
        return removed;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.REMOVE_ELEMENT;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return element;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.PUT_IF_ABSENT;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return null;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.PUT;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return null;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.PUT_WITH_WRITER;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return replaced;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.REPLACE_ELEMENT;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return oldReplacedElement;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.REPLACE;
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
          cache.getReadStats().incrementTotalExceptionCount();
        } catch (RejoinCacheException rce) {
          cache.getReadStats().incrementTotalExceptionCount();
        }
        if (cache.isStatisticsEnabled()) {
          long end = now();
          cache.getWriteStats().add(end - start);
        }
        return null;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.UPDATE;
      }
    };
  }

}
