package org.terracotta.ehcache.testing.objectgenerator;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public class StringGenerator implements ObjectGenerator {

  private final Class clazz;
  private final int length;
  private final Random rnd;

  private StringGenerator(final Class clazz, final int length) {
    this(clazz, length, -1L);
  }

  public StringGenerator(final Class clazz, final int length, final long seed) {
    this.clazz = clazz;
    this.length = length;
    if (seed == -1L) {
      this.rnd = new Random();
    } else {
      this.rnd = new Random(seed);
    }
  }

  public Object generate(long seed) {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      if (clazz == Long.class) {
        sb.append(Long.toString(seed));
      } else if (clazz == Byte.class) {
        sb.append(Byte.toString((byte)seed));
      } else if (clazz == String.class) {
        sb.append(getRndChar());
      }
    }
    return sb.toString();
  }

  private char getRndChar() {
    int rnd = this.rnd.nextInt(52);
    char base = (rnd < 26) ? 'A' : 'a';
    return (char)(base + rnd % 26);
  }

  /**
   * Returns a Generator which will generate a String representing a number
   */
  public static ObjectGenerator integers() {
    return new StringGenerator(Long.class, 1);
  }

  /**
   * Returns a Generator which will generate a random String
   *
   * @param length the length of the returned String
   */
  public static ObjectGenerator randomString(int length) {
    return new StringGenerator(String.class, length);
  }

  /**
   * Returns a Generator which will generate a number of characters
   *
   * @param length the length of the returned String
   */
  public static ObjectGenerator chars(int length) {
    return new StringGenerator(Byte.class, length);
  }

  /**
   * Returns a Generator which will generate a random String with the given length and given seed
   *
   * @param length the length of the returned String
   */
  public static ObjectGenerator randomString(final long seed, final int length) {
    return new StringGenerator(String.class, length, seed);
  }

}
