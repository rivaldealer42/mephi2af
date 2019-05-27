
package com.mephi2AF.android.apps.authenticator;

import android.os.Handler;

import com.mephi2AF.android.apps.authenticator.TotpCounter;
import com.mephi2AF.android.apps.authenticator.Utilities;


class TotpCountdownTask implements Runnable {
  private final TotpCounter mCounter;
  private final TotpClock mClock;
  private final long mRemainingTimeNotificationPeriod;
  private final Handler mHandler = new Handler();

  private long mLastSeenCounterValue = Long.MIN_VALUE;
  private boolean mShouldStop;
  private Listener mListener;

  /**
   * Listener notified of changes to the time remaining until the counter value changes.
   */
  interface Listener {

    /**
     * Invoked when the time remaining till the TOTP counter changes its value.
     *
     * @param millisRemaining time (milliseconds) remaining.
     */
    void onTotpCountdown(long millisRemaining);

    /** Invoked when the TOTP counter changes its value. */
    void onTotpCounterValueChanged();
  }


  TotpCountdownTask(TotpCounter counter, TotpClock clock, long remainingTimeNotificationPeriod) {
    mCounter = counter;
    mClock = clock;
    mRemainingTimeNotificationPeriod = remainingTimeNotificationPeriod;
  }


  void setListener(Listener listener) {
    mListener = listener;
  }


  void startAndNotifyListener() {
    if (mShouldStop) {
      throw new IllegalStateException("Task already stopped and cannot be restarted.");
    }

    run();
  }


  void stop() {
    mShouldStop = true;
  }

  @Override
  public void run() {
    if (mShouldStop) {
      return;
    }

    long now = mClock.currentTimeMillis();
    long counterValue = getCounterValue(now);
    if (mLastSeenCounterValue != counterValue) {
      mLastSeenCounterValue = counterValue;
      fireTotpCounterValueChanged();
    }
    fireTotpCountdown(getTimeTillNextCounterValue(now));

    scheduleNextInvocation();
  }

  private void scheduleNextInvocation() {
    long now = mClock.currentTimeMillis();
    long counterValueAge = getCounterValueAge(now);
    long timeTillNextInvocation =
        mRemainingTimeNotificationPeriod - (counterValueAge % mRemainingTimeNotificationPeriod);
    mHandler.postDelayed(this, timeTillNextInvocation);
  }

  private void fireTotpCountdown(long timeRemaining) {
    if ((mListener != null) && (!mShouldStop)) {
      mListener.onTotpCountdown(timeRemaining);
    }
  }

  private void fireTotpCounterValueChanged() {
    if ((mListener != null) && (!mShouldStop)) {
      mListener.onTotpCounterValueChanged();
    }
  }

  /**
   * Gets the value of the counter at the specified time instant.
   *
   * @param time time instant (milliseconds since epoch).
   */
  private long getCounterValue(long time) {
    return mCounter.getValueAtTime(Utilities.millisToSeconds(time));
  }

  /**
   * Gets the time remaining till the counter assumes its next value.
   *
   * @param time time instant (milliseconds since epoch) for which to perform the query.
   *
   * @return time (milliseconds) till next value.
   */
  private long getTimeTillNextCounterValue(long time) {
    long currentValue = getCounterValue(time);
    long nextValue = currentValue + 1;
    long nextValueStartTime = Utilities.secondsToMillis(mCounter.getValueStartTime(nextValue));
    return nextValueStartTime - time;
  }


  private long getCounterValueAge(long time) {
    return time - Utilities.secondsToMillis(mCounter.getValueStartTime(getCounterValue(time)));
  }
}
