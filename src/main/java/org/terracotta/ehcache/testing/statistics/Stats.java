/*
 *  Copyright Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.terracotta.ehcache.testing.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicLong;

public class Stats {
	private static final NumberFormat nf = NumberFormat.getInstance();
	private static final Logger log = LoggerFactory.getLogger(Stats.class);
    private static final boolean enableHisto = Boolean.parseBoolean(System.getProperty("enable.histo", "false"));

	private Stats period = null;
	private AtomicLong transactionsCount;

	private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
	private AtomicLong totalExceptionCount = new AtomicLong();
	private AtomicLong endTime = null;
	private AtomicLong totalTxLatency;
	private double minLatency, maxLatency;

	private Histogram histo;

	public Stats() {
		defaultInit();
	}

	public Stats(Stats stat) {
    if (stat != null) {
      init(stat.getTxnCount(), stat.totalTxLatency.get(),
          stat.minLatency, stat.maxLatency, stat.startTime,
          stat.endTime, stat.getTotalExceptionCount(),
          stat.getHisto());
    } else {
      defaultInit();
    }
  }

	private void defaultInit() {
    init(0, 0, Double.MAX_VALUE, Double.MIN_VALUE, null, null, 0, null);
  }

  private void init(long transactionsCount, long total, double minLatency,
                    double maxLatency, AtomicLong startTime, AtomicLong endTime,
                    long totalExceptions, Histogram hist) {

    this.transactionsCount = new AtomicLong(transactionsCount);
		this.totalTxLatency = new AtomicLong(total);
		this.totalExceptionCount = new AtomicLong(totalExceptions);
		this.minLatency = minLatency;
		this.maxLatency = maxLatency;

		if (startTime != null)
			this.startTime.set(startTime.get());

		if (endTime != null)
			this.endTime = new AtomicLong(endTime.get());

		this.histo = new Histogram();
        if (enableHisto){
            if (hist != null)
                this.histo.add(hist);
        }
	}

	/**
	 * Add {@link Stats} to the current.
	 *
	 * @param stat
	 * @return {@link this}
	 */
	public Stats add(Stats stat) {
		this.transactionsCount.addAndGet(stat.getTxnCount());
		this.totalTxLatency.addAndGet(stat.totalTxLatency.get());

		if (stat.minLatency < this.minLatency)
			this.minLatency = stat.minLatency;

		if (stat.maxLatency > this.maxLatency)
			this.maxLatency = stat.maxLatency;

		// whichever started earlier
		if (stat.startTime.get() < this.startTime.get())
			this.startTime.set(stat.startTime.get());

		// whichever finished later
		if (stat.endTime != null) {
			if (this.endTime == null)
				this.endTime = new AtomicLong(stat.endTime.get());
			else if (stat.endTime.get() > this.endTime.get())
				this.endTime.set(stat.endTime.get());
		}

        if (enableHisto)
            this.histo.add(stat.histo);
		this.totalExceptionCount.addAndGet(stat.totalExceptionCount.get());

		return this;
	}

	/**
	 * Add transaction length
	 *
	 * @param txLength
	 *            transaction length
	 */
	public void add(long txLength) {
	    if (endTime != null)
	        throw new IllegalStateException("Stats has been finalized...!!");
		if (txLength > 32000000000L) {    // 32 seconds in nanoseconds
			log.warn("stat transaction length exceeds 32 secs, txLength = {}", txLength);
		}
		transactionsCount.incrementAndGet();
		totalTxLatency.addAndGet(txLength);
		if (txLength < minLatency) {
			minLatency = txLength;
		}
		if (txLength > maxLatency) {
			maxLatency = txLength;
		}

		if (period == null) {
			period = new Stats();
		}

        if (enableHisto){
            histo.add(txLength);
            period.histo.add(txLength);
        }

		period.transactionsCount.incrementAndGet();
		period.totalTxLatency.addAndGet(txLength);
		if (txLength < period.minLatency)
			period.minLatency = txLength;
		if (txLength > period.maxLatency)
			period.maxLatency = txLength;
	}

	public void incrementTotalExceptionCount() {
    if (period != null)
      period.totalExceptionCount.incrementAndGet();
    totalExceptionCount.incrementAndGet();
  }

	/**
	 * @return average latency
	 */
	public double getAvgLatency() {
		if (transactionsCount.get() > 0)
			return (double) totalTxLatency.get() / transactionsCount.get();
		return 0;
	}

	/**
	 *
	 * @return max latency
	 */
	public double getMaxLatency() {
		if (maxLatency == Double.MIN_VALUE)
			return Double.NaN;
		return maxLatency;
	}

	/**
	 *
	 * @return min latency
	 */
	public double getMinLatency() {
		if (minLatency == Double.MAX_VALUE)
			return Double.NaN;
		return minLatency;
	}

	/**
	 * resets the stats
	 */
	public synchronized void reset() {
		transactionsCount.set(0);
		totalTxLatency.set(0);
		totalExceptionCount.set(0);
		minLatency = Double.MAX_VALUE;
		maxLatency = Double.MIN_VALUE;
		this.startTime.set(System.currentTimeMillis());
		this.endTime = null;
		this.histo = new Histogram();
	}

	/**
	 * @return total txn count
	 */
	public long getTxnCount() {
		return transactionsCount.get();
	}

	public long getThroughput() {
		long end = (endTime != null) ? endTime.get() : System.currentTimeMillis();
		long time = end - this.startTime.get();
		if (time == 0)
			time = 1;
		return getTxnCount() * 1000 / time;
	}

	public Stats getPeriodStats() {
		Stats p = new Stats(period);
		if (period != null) {
			period.reset();
		}
		return p;
	}

	public void finalise() {
		if (endTime != null)
			// stats can be finalized only once, but can be called multiple
			// times
			return;
		this.endTime = new AtomicLong();
		this.endTime.set(System.currentTimeMillis());
	}

  public long getTotalExceptionCount() {
    return totalExceptionCount.get();
  }

  public Histogram getHisto() {
    return histo;
  }

  @Override
	public String toString() {
    return String
        .format("Txns: %s, TPS: %s, Latency(ms): Avg: %s, Min: %s, Max: %s, TotalExceptionCount: %s",
            nf.format(this.getTxnCount()),
            nf.format(this.getThroughput()),
            nf.format(this.getAvgLatency()),
            nf.format(this.getMinLatency()),
            nf.format(this.getMaxLatency()),
            nf.format(this.getTotalExceptionCount())
        );
  }

}
