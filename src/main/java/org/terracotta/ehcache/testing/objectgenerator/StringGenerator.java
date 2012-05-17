package org.terracotta.ehcache.testing.objectgenerator;

import java.util.UUID;

public class StringGenerator implements ObjectGenerator {

  private Class clazz;
  private int length;

  private StringGenerator(final Class clazz, final int length) {
    this.clazz = clazz;
    this.length = length;
  }

  public Object generate(int seed) {
    String st = null;
    if (clazz == Integer.class) {
      st = Integer.toString(seed);
    } else if (clazz == Byte.class) {
      st = Byte.toString((byte)seed);
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {

      if (clazz != null) {
        sb.append(st);
      } else {
        String myRandom = UUID.randomUUID().toString();
        sb.append(myRandom.substring(0, 1));
      }
    }
    return sb.toString();
  }

  /**
   * Returns a Generator which will generate a String representing a number
   */
  public static ObjectGenerator integers() {
    return new StringGenerator(Integer.class, 1);
  }

  /**
   * Returns a Generator which will generate a random String
   *
   * @param length the length of the returned String
   */
  public static ObjectGenerator randomString(int length) {
    return new StringGenerator(null, length);
  }

  /**
   * Returns a Generator which will generate a number of characters
   *
   * @param length the length of the returned String
   */
  public static ObjectGenerator chars(int length) {
    return new StringGenerator(Byte.class, length);
  }

}
