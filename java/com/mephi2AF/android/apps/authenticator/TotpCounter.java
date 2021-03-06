
package com.mephi2AF.android.apps.authenticator;


public class TotpCounter {

  /** Interval of time (seconds) between successive changes of this counter's value. */
  private final long mTimeStep;

  /**
   * Earliest time instant (seconds since UNIX epoch) at which this counter assumes the value of
   * {@code 0}.
   */
  private final long mStartTime;

  /**
   * Constructs a new {@code TotpCounter} that starts with the value {@code 0} at time instant
   * {@code 0} (seconds since UNIX epoch) and increments its value with the specified frequency.
   *
   * @param timeStep interval of time (seconds) between successive changes of this counter's value.
   */
  public TotpCounter(long timeStep) {
    this(timeStep, 0);
  }

  /**
   * Constructs a new {@code TotpCounter} that starts with the value {@code 0} at the specified
   * time and increments its value with the specified frequency.
   *
   * @param timeStep interval of time (seconds) between successive changes of this counter's value.
   * @param startTime the earliest time instant (seconds since UNIX epoch) at which this counter
   *        assumes the value {@code 0}.
   */
  public TotpCounter(long timeStep, long startTime) {
    if (timeStep < 1) {
      throw new IllegalArgumentException("Time step must be positive: " + timeStep);
    }
    assertValidTime(startTime);

    mTimeStep = timeStep;
    mStartTime = startTime;
  }

  /**
   * Gets the frequency with which the value of this counter changes.
   *
   * @return interval of time (seconds) between successive changes of this counter's value.
   */
  public long getTimeStep() {
    return mTimeStep;
  }

  /**
   * Gets the earliest time instant at which this counter assumes the value {@code 0}.
   *
   * @return time (seconds since UNIX epoch).
   */
  public long getStartTime() {
    return mStartTime;
  }

  /**
   * Gets the value of this counter at the specified time.
   *
   * @param time time instant (seconds since UNIX epoch) for which to obtain the value.
   *
   * @return value of the counter at the {@code time}.
   */
  public long getValueAtTime(long time) {
    assertValidTime(time);


    long timeSinceStartTime = time - mStartTime;
    if (timeSinceStartTime >= 0) {
      return timeSinceStartTime / mTimeStep;
    } else {
      return (timeSinceStartTime - (mTimeStep - 1)) / mTimeStep;
    }
  }

  /**
   * Gets the time when the counter assumes the specified value.
   *
   * @param value value.
   *
   * @return earliest time instant (seconds since UNIX epoch) when the counter assumes the value.
   */
  public long getValueStartTime(long value) {
    return mStartTime + (value * mTimeStep);
  }

  private static void assertValidTime(long time) {
    if (time < 0) {
      throw new IllegalArgumentException("Negative time: " + time);
    }
  }
}
