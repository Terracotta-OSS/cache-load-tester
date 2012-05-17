package org.terracotta.ehcache.testing.driver;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcache.testing.driver.CacheAccessor.IndividualCacheAccessor;


public abstract class AccessPattern implements Runnable {
	/**
	 * SPIKE : returns {@link SpikeAccessPattern} <br>
	 * WAVE: returns {@link WaveAccessPattern} <br>
	 * NORMAL: returns {@link NormalAccessPattern}
	 *
	 * @author Himadri Singh
	 */
	public enum Pattern { SPIKE, WAVE, NORMAL };

	private static final Logger log = LoggerFactory.getLogger(AccessPattern.class);
	private static final long DELAY_IN_MICROS = 2 * 1000;

	protected List<IndividualCacheAccessor> accessorsList;
	protected int duration = -1;
	protected int interval = -1;
	protected AtomicBoolean isRunning = new AtomicBoolean(true);

	/**
	 * Sets duration of the transition of the access pattern
	 *
	 * @param duration time in seconds
	 * @return this
	 */
	AccessPattern setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	/**
	 * Sets interval of repeatition of transtion of access pattern.
	 *
	 * @param interval time in secs
	 * @return this
	 */

	AccessPattern setInterval(int interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * Sets the list of {@link IndividualCacheAccessor} to be accessed in set pattern
	 *
	 * @param accessors list of cache accessors
	 * @return this
	 */
	AccessPattern setAccessors(List<IndividualCacheAccessor> accessors) {
		this.accessorsList = accessors;
		return this;
	}

	/**
	 * Creates a pattern depending on {@link Pattern} type
	 *
	 * @param pattern
	 * @return this
	 */
	static AccessPattern create(Pattern pattern) {
		switch (pattern){
		case SPIKE:
			return new SpikeAccessPattern();
		case WAVE:
			return new WaveAccessPattern();
		case NORMAL:
		default:
			return new NormalAccessPattern();
		}
	}

	/**
	 * introduces delay to all the accessors
	 * @param nanos delay in nanoseconds
	 */
	protected void introduceDelay(long nanos){
		for (IndividualCacheAccessor accessor : accessorsList){
			accessor.introduceDelay(nanos);
		}
	}

	protected void stop(){
		isRunning.set(false);
	}

	/**
	 * Does nothing
	 *
	 * @author Himadri Singh
	 *
	 */
	static class NormalAccessPattern extends AccessPattern {
		public void run() {
			// Do nothing.
		}
	}

	/**
	 * One of the accessors will be running highest load, then next accessors
	 * will gradually increase over <tt>durationInSecs</tt> while previous accessor should be
	 * going reducing the load. The process repeats after every <tt>intervalInSecs</tt>
	 *
	 * @author Himadri Singh
	 *
	 */
	static class WaveAccessPattern extends AccessPattern {

		private static final int GRADUAL_STEPS = 10;

		public void run() {
			if (accessorsList == null)
				throw new IllegalStateException("Accessors can't be null!!");

			IndividualCacheAccessor prev = null;
			introduceDelay(DELAY_IN_MICROS);
			long stepWait = TimeUnit.MICROSECONDS.convert(duration, TimeUnit.SECONDS)/ GRADUAL_STEPS;
			long stepDelay = DELAY_IN_MICROS / GRADUAL_STEPS;

			while (isRunning.get()) {
				// reduce the load of all accessors

				for (IndividualCacheAccessor accessor : accessorsList) {
					try {
						TimeUnit.SECONDS.sleep(interval);
					} catch (InterruptedException e) {
						return;
					}

					long increasing_delay = 0;
					long decreasing_delay = DELAY_IN_MICROS;


					long start = System.currentTimeMillis();
					for (int i = 0; i < GRADUAL_STEPS; i++){
						if (prev != null){
							prev.introduceDelay(increasing_delay);
							increasing_delay += stepDelay;
						}
						accessor.introduceDelay(decreasing_delay);
						decreasing_delay -= stepDelay;
						try { TimeUnit.MICROSECONDS.sleep(stepWait);} catch (InterruptedException e) { return; }
					}
					accessor.introduceDelay(0);
					prev = accessor;
					long end = System.currentTimeMillis();
					log.info("Finished in ..." + (end - start));
				}
			}
		}

	}

	/**
	 * One of the accessors with reduce the load for <tt>durationInSecs</tt>
	 * after every <tt>intervalInSecs</tt>. Resetting the load to normal after
	 * each spike.
	 *
	 * @author Himadri Singh
	 *
	 */
	static class SpikeAccessPattern extends AccessPattern {

		public void run() {
			if (accessorsList == null)
				throw new IllegalStateException("Accessors can't be null!!");

			while (isRunning.get()){
				for (IndividualCacheAccessor accessor : accessorsList){
					try {
						TimeUnit.SECONDS.sleep(interval);
					} catch (InterruptedException e) {
						return;
					}
					accessor.introduceDelay(DELAY_IN_MICROS);
					try {
						TimeUnit.SECONDS.sleep(duration);
					} catch (InterruptedException e) {
						return;
					}
					introduceDelay(0);
				}
			}
		}
	}
}
