package org.terracotta.ehcache.testing.sequencegenerator;

import java.util.Random;

public class RandomSequenceGenerator implements SequenceGenerator {

  private final Distribution distribution;
  private final int minimum;
  private final int maximum;
  private final int width;
  
  public RandomSequenceGenerator(Distribution distribution, int min, int max, int width) {
    this.distribution = distribution;
    this.minimum = min;
    this.maximum = max;
    this.width = width;
  }

  public Sequence createSequence() {
    return new RandomSequence(distribution, minimum, maximum, width);
  }
  
  static class RandomSequence implements Sequence {

    private final Random rndm = new Random();
    private final Distribution distribution;
    private final int minimum;
    private final int maximum;
    private final int width;
    
    public RandomSequence(Distribution distribution, int minimum, int maximum, int width) {
      this.distribution = distribution;
      this.minimum = minimum;
      this.maximum = maximum;
      this.width = width;
    }
    
    public int next() {
      return distribution.generate(rndm, minimum, maximum, width);
    }
  }
}
