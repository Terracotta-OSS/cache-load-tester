package org.terracotta.ehcache.testing.validator;

import java.lang.reflect.Array;

import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;

public class EqualityValidation implements Validation {

  public Validator createValidator(ObjectGenerator valueGenerator) {
    return new EqualityValidator(valueGenerator);
  }

  static class EqualityValidator implements Validator {

    private final ObjectGenerator valueGenerator;

    public EqualityValidator(ObjectGenerator valueGenerator) {
      this.valueGenerator = valueGenerator;
    }
    public void validate(long seed, Object value) {
      Object expected = valueGenerator.generate(seed);
      if (value == null) {
        if (expected != null) {
          throw new AssertionError();
        }
      } else if (value.getClass().isArray() && expected.getClass().isArray()) {
        if (Array.getLength(expected) != Array.getLength(value)) {
          throw new AssertionError();
        } else {
          for (int i = 0; i < Array.getLength(expected); i++) {
            if (!Array.get(expected, i).equals(Array.get(value, i))) {
              throw new AssertionError();
            }
          }
        }
      } else if (!expected.equals(value)) {
        throw new AssertionError();
      }
    }

  }
}
