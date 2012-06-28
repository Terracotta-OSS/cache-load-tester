package org.terracotta.ehcache.testing.sequencegenerator;

public interface SequenceGenerator {

  public Sequence createSequence();

  public interface Sequence {
    public long next();
  }
}
