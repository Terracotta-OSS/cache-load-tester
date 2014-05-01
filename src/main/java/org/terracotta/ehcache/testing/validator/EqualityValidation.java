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
          throw new AssertionError("Value to be validated is null and validation value is not. " +
                                   "(seed=[" + seed + "], validation value=[" + expected.toString() + "])");
        }
      } else if (value.getClass().isArray() && expected.getClass().isArray()) {
        if (Array.getLength(expected) != Array.getLength(value)) {
          throw new AssertionError("Value to be validated is an array and is not of the same size of the validation value.");
        } else {
          for (int i = 0; i < Array.getLength(expected); i++) {
            if (!Array.get(expected, i).equals(Array.get(value, i))) {
              throw new AssertionError("Value to be validated is an array and is not equal to the validation value.");
            }
          }
        }
      } else if (!expected.equals(value)) {
        throw new AssertionError("Value to be validated is different from validation value. " +
                                 "(seed=[" + seed + "], value=["+value.toString()+"], validation value=[" + expected.toString() + "])");
      }
    }

  }
}
