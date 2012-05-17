package org.terracotta.ehcache.testing.validator;

import org.terracotta.ehcache.testing.objectgenerator.ObjectGenerator;

public interface Validation {

  public Validator createValidator(ObjectGenerator valueGenerator);

  public interface Validator {

    public void validate(int seed, Object value);
  }
}
