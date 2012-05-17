package org.terracotta.ehcache.testing.sequencegenerator;

import java.util.concurrent.atomic.AtomicInteger;

public class SequentialSequenceGenerator implements SequenceGenerator {

  private final int offset;

  public SequentialSequenceGenerator(final int offset) {
    this.offset = offset;
  }

  public Sequence createSequence() {
    return new SequentialSequence(offset);
  }

  public int getOffset() {
	return offset;
  }

  static class SequentialSequence implements Sequence {

    private final AtomicInteger next;

    public SequentialSequence(final int offset) {
      next = new AtomicInteger(offset);
    }

    public int next() {
      return next.getAndIncrement();
    }
  }
}
