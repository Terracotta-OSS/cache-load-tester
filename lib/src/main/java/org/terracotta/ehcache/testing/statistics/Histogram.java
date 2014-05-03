package org.terracotta.ehcache.testing.statistics;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.math.LongRange;

public class Histogram {
	private static final NumberFormat nf = NumberFormat.getInstance();

	private final AtomicLong BUCKET_0_10_COUNT = new AtomicLong();
	private final AtomicLong BUCKET_10_50_COUNT = new AtomicLong();
	private final AtomicLong BUCKET_50_100_COUNT = new AtomicLong();
	private final AtomicLong BUCKET_100_200_COUNT = new AtomicLong();
	private final AtomicLong BUCKET_200_500_COUNT = new AtomicLong();
	private final AtomicLong BUCKET_500_1000_COUNT = new AtomicLong();
	private final AtomicLong BUCKET_1000_5000_COUNT = new AtomicLong();
	private final AtomicLong BUCKET_5000_PLUS_COUNT = new AtomicLong();
	private final AtomicLong TOTAL = new AtomicLong();

	private static final LongRange Range_0_10 = new LongRange(Long.MIN_VALUE,
			10);
	private static final LongRange Range_10_50 = new LongRange(10, 50);
	private static final LongRange Range_50_100 = new LongRange(50, 100);
	private static final LongRange Range_100_200 = new LongRange(100, 200);
	private static final LongRange Range_200_500 = new LongRange(200, 500);
	private static final LongRange Range_500_1000 = new LongRange(500, 1000);
	private static final LongRange Range_1000_5000 = new LongRange(1000, 5000);

	public void add(long value) {
		if (Range_0_10.containsLong(value)) {
			BUCKET_0_10_COUNT.incrementAndGet();
		} else if (Range_10_50.containsLong(value)) {
			BUCKET_10_50_COUNT.incrementAndGet();
		} else if (Range_50_100.containsLong(value)) {
			BUCKET_50_100_COUNT.incrementAndGet();
		} else if (Range_100_200.containsLong(value)) {
			BUCKET_100_200_COUNT.incrementAndGet();
		} else if (Range_200_500.containsLong(value)) {
			BUCKET_200_500_COUNT.incrementAndGet();
		} else if (Range_500_1000.containsLong(value)) {
			BUCKET_500_1000_COUNT.incrementAndGet();
		} else if (Range_1000_5000.containsLong(value)) {
			BUCKET_1000_5000_COUNT.incrementAndGet();
		} else {
			BUCKET_5000_PLUS_COUNT.incrementAndGet();
		}
		TOTAL.incrementAndGet();
	}

	public Histogram add(Histogram med) {
		this.BUCKET_0_10_COUNT.addAndGet(med.BUCKET_0_10_COUNT.get());
		this.BUCKET_10_50_COUNT.addAndGet(med.BUCKET_10_50_COUNT.get());
		this.BUCKET_50_100_COUNT.addAndGet(med.BUCKET_50_100_COUNT.get());
		this.BUCKET_100_200_COUNT.addAndGet(med.BUCKET_100_200_COUNT.get());
		this.BUCKET_200_500_COUNT.addAndGet(med.BUCKET_200_500_COUNT.get());
		this.BUCKET_500_1000_COUNT.addAndGet(med.BUCKET_500_1000_COUNT.get());
		this.BUCKET_1000_5000_COUNT.addAndGet(med.BUCKET_1000_5000_COUNT.get());
		this.BUCKET_5000_PLUS_COUNT.addAndGet(med.BUCKET_5000_PLUS_COUNT.get());
		this.TOTAL.addAndGet(med.TOTAL.get());
		return this;
	}

	public double calculatePercentage(AtomicLong value) {
		return (TOTAL.get() > 0) ? ((((double) value.get()) / ((double) TOTAL
				.get())) * 100.0f) : 0f;

	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("0-10 frequency count = ")
				.append(nf.format(BUCKET_0_10_COUNT.get()))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_0_10_COUNT))).append("\n");
		sb.append("10-50 frequency count = ")
				.append(nf.format(BUCKET_10_50_COUNT))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_10_50_COUNT))).append("\n");
		sb.append("50-100 frequency count = ")
				.append(nf.format(BUCKET_50_100_COUNT))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_50_100_COUNT))).append("\n");
		sb.append("100-200 frequency count = ")
				.append(nf.format(BUCKET_100_200_COUNT))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_100_200_COUNT)))
				.append("\n");
		sb.append("200-500 frequency count = ")
				.append(nf.format(BUCKET_200_500_COUNT))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_200_500_COUNT)))
				.append("\n");
		sb.append("500-1000 frequency count = ")
				.append(nf.format(BUCKET_500_1000_COUNT))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_500_1000_COUNT)))
				.append("\n");
		sb.append("1000-5000 frequency count = ")
				.append(nf.format(BUCKET_1000_5000_COUNT))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_1000_5000_COUNT)))
				.append("\n");
		sb.append("5000-PLUS frequency count = ")
				.append(nf.format(BUCKET_5000_PLUS_COUNT))
				.append(String.format(" percentage = %.1f",
						calculatePercentage(BUCKET_5000_PLUS_COUNT)))
				.append("\n");
		return sb.toString();
	}

	public long getBUCKET_0_10_COUNT() {
		return BUCKET_0_10_COUNT.get();
	}

	public long getBUCKET_10_50_COUNT() {
		return BUCKET_10_50_COUNT.get();
	}

	public long getBUCKET_50_100_COUNT() {
		return BUCKET_50_100_COUNT.get();
	}

	public long getBUCKET_100_200_COUNT() {
		return BUCKET_100_200_COUNT.get();
	}

	public long getBUCKET_200_500_COUNT() {
		return BUCKET_200_500_COUNT.get();
	}

	public long getBUCKET_500_1000_COUNT() {
		return BUCKET_500_1000_COUNT.get();
	}

	public long getBUCKET_1000_5000_COUNT() {
		return BUCKET_1000_5000_COUNT.get();
	}

	public long getBUCKET_5000_PLUS_COUNT() {
		return BUCKET_5000_PLUS_COUNT.get();
	}

	public long getTOTAL() {
		return TOTAL.get();
	}

}
