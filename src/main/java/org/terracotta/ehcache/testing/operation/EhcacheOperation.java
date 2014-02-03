package org.terracotta.ehcache.testing.operation;

import net.sf.ehcache.Element;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.validator.Validation;

public class EhcacheOperation {

  public static CacheOperation get(final double ratio) {
    return new CacheOperation(ratio) {

      @Override
      public Object exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {

        Object key = keyGenerator.generate(seed);
        Object value = cache.get(key);
        if (Validation.Mode.STRICT.equals(getValidationMode())) {
          if (validator == null) {
            throw new AssertionError("Validator is null");
          }
          validator.validate(seed, value);
        } else {
          if (value == null) {
            cache.put(key, valueGenerator.generate(seed));
          } else if (validator != null) {
            validator.validate(seed, value);
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
      public Boolean exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        return cache.remove(keyGenerator.generate(seed));
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
      public Boolean exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        return cache.removeElement(keyGenerator.generate(seed), valueGenerator.generate(seed));
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
      public Object exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        return cache.putIfAbsent(keyGenerator.generate(seed), valueGenerator.generate(seed));
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
      public Void exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        cache.put(keyGenerator.generate(seed), valueGenerator.generate(seed));
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
      public Void exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        cache.putWithWriter(keyGenerator.generate(seed), valueGenerator.generate(seed));
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
      public Boolean exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        return cache.replaceElement(keyGenerator.generate(seed), valueGenerator.generate(seed),
            keyGenerator.generate(seed), valueGenerator.generate(seed));
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
      public Element exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        return cache.replace(keyGenerator.generate(seed), valueGenerator.generate(seed));
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
      public Void exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator) {
        cache.put(keyGenerator.generate(seed), valueGenerator.generate(seed));
        return null;
      }

      @Override
      public OPERATIONS getName() {
        return OPERATIONS.UPDATE;
      }
    };
  }

}
