package org.terracotta.ehcache.testing.operation;

import org.terracotta.ehcache.testing.cache.CacheWrapper;
import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;
import org.terracotta.ehcache.testing.validator.Validation;

public abstract class CacheOperation<T> {

  private Validation.Mode validationMode;

  public enum OPERATIONS {
    GET, UPDATE, REMOVE, REMOVE_ELEMENT, REPLACE, REPLACE_ELEMENT, PUT, PUT_IF_ABSENT, PUT_WITH_WRITER;
  }

  CacheWrapper cache;
  Double ratio;

  private CacheOperation() {
  }

  protected CacheOperation(final Double ratio) {
    this.ratio = ratio;
  }

  protected CacheOperation(final Double ratio, final CacheWrapper cache) {
    this.ratio = ratio;
    this.cache = cache;
  }

  public void setCache(CacheWrapper cache) {
    this.cache = cache;
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

  public abstract T exec(final long seed, final ObjectGenerator keyGenerator, final ObjectGenerator valueGenerator, final Validation.Validator validator);

  public abstract OPERATIONS getName();
}
