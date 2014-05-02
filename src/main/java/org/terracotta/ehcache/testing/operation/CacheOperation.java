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

import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.validator.Validation;

public abstract class CacheOperation<T> {

  private Validation.Mode validationMode;

  public enum OPERATIONS {
    GET, UPDATE, REMOVE, REMOVE_ELEMENT, REPLACE, REPLACE_ELEMENT, PUT, PUT_IF_ABSENT, PUT_WITH_WRITER, PUT_CONTROLLED_THROUGHPUT;
  }

  Double ratio;

  private CacheOperation() {
  }

  protected CacheOperation(final Double ratio) {
    this.ratio = ratio;
  }

  protected CacheOperation(final Double ratio, final GenericCacheWrapper cache) {
    this.ratio = ratio;
  }

  public void setValidationMode(final Validation.Mode validationMode) {
    this.validationMode = validationMode;
  }

  public Validation.Mode getValidationMode() {
    return validationMode;
  }

  public Double getRatio() {
    return ratio;
  }

  public void setRatio(final Double ratio) {
    this.ratio = ratio;
  }

  protected static long now() {
    return System.nanoTime();
  }

  public abstract T exec(final GenericCacheWrapper cacheWrapper, final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator);

  public abstract OPERATIONS getName();
}
