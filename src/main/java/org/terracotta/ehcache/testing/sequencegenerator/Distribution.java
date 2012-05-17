package org.terracotta.ehcache.testing.sequencegenerator;

import java.util.Random;

public enum Distribution {
  FLAT {

    @Override
    public int generate(Random rndm, int minimum, int maximum, int width) {
      return rndm.nextInt(maximum - minimum) + minimum;
    }
  },
  GAUSSIAN {
    @Override
    public int generate(Random rndm, int minimum, int maximum, int width) {
      while (true) {
        int candidate = (int) ((rndm.nextGaussian() * width) + (((double) maximum + minimum) / 2));
        if (candidate >= minimum && candidate < maximum) {
          return candidate;
        }
      }
    }
  };

  public abstract int generate(Random rndm, int minimum, int maximum, int width);
}
