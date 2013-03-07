package org.terracotta.ehcache.testing.objectgenerator;

import java.io.Serializable;

/**
 * Made by aurbrsz / 7/11/11 - 14:48
 *
 * TODO : generate this through ASM
 */
public class Triple implements Serializable {

  private Object one;
  private Object two;
  private Object three;

  public Object getOne() {
    return one;
  }

  public void setOne(final Object one) {
    this.one = one;
  }

  public Object getTwo() {
    return two;
  }

  public void setTwo(final Object two) {
    this.two = two;
  }

  public Object getThree() {
    return three;
  }

  public void setThree(final Object three) {
    this.three = three;
  }

}
