package org.terracotta.ehcache.testing.sequencegenerator;

import java.util.concurrent.atomic.AtomicLong;

public class SequentialSequenceGenerator implements SequenceGenerator {

  private final long offset;

  public SequentialSequenceGenerator(final long offset) {
    this.offset = offset;
  }

  public Sequence createSequence() {
    return new SequentialSequence(offset);
  }

  public long getOffset() {
	return offset;
  }

  static class SequentialSequence implements Sequence {

    private final AtomicLong next;

    public SequentialSequence(final long offset) {
      next = new AtomicLong(offset);
    }

    public long next() {
      return next.getAndIncrement();
    }
  }
}
