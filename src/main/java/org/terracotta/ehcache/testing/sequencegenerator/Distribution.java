package org.terracotta.ehcache.testing.sequencegenerator;

import java.util.Random;

public enum Distribution {
  FLAT {

    @Override
    public long generate(Random rndm, long minimum, long maximum, long width) {
      return (rndm.nextLong() % (maximum - minimum)) + minimum;
    }
  },
  GAUSSIAN {
    @Override
    public long generate(Random rndm, long minimum, long maximum, long width) {
      while (true) {
        long candidate = (long) ((rndm.nextGaussian() * width) + (((double) maximum + minimum) / 2));
        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
    }
  };

  public abstract long generate(Random rndm, long minimum, long maximum, long width);
}
