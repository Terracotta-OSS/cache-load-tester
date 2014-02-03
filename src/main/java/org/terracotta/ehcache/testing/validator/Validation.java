package org.terracotta.ehcache.testing.validator;

import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;

public interface Validation {

  public enum Mode {STRICT, UPDATE}

  public Validator createValidator(ObjectGenerator valueGenerator);

  public interface Validator {

    public void validate(long seed, Object value);
  }
}
