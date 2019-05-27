
package com.mephi2AF.android.apps.authenticator.timesync;

import com.mephi2AF.android.apps.authenticator.RunOnThisLooperThreadExecutor;
import com.mephi2AF.android.apps.authenticator.TotpClock;
import com.mephi2AF.android.apps.authenticator.Utilities;
import com.mephi2AF.android.apps.authenticator.timesync.NetworkTimeProvider;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class SyncNowController {
  enum Result {
    TIME_CORRECTED,
    TIME_ALREADY_CORRECT,
    CANCELLED_BY_USER,
    ERROR_CONNECTIVITY_ISSUE,
  }

  /** Presentation layer. */
  interface Presenter {

    /** Invoked when the controller starts up. */
    void onStarted();

    /** Invoked when the controller is finished. */
    void onDone(Result result);
  }

  private enum State {
    NOT_STARTED,
    IN_PROGRESS,
    DONE,
  }

  private static final String LOG_TAG = "TimeSync";

  private final TotpClock mTotpClock;
  private final NetworkTimeProvider mNetworkTimeProvider;
  private final Executor mBackgroundExecutor;
  private final Executor mCallbackFromBackgroundExecutor;
  private final boolean mBackgroundExecutorServiceOwnedByThisController;

  private Presenter mPresenter;
  private State mState = State.NOT_STARTED;
  private Result mResult;

  // @VisibleForTesting
  SyncNowController(
      TotpClock totpClock,
      NetworkTimeProvider networkTimeProvider,
      Executor backgroundExecutor,
      boolean backgroundExecutorServiceOwnedByThisController,
      Executor callbackFromBackgroundExecutor) {
    mTotpClock = totpClock;
    mNetworkTimeProvider = networkTimeProvider;
    mBackgroundExecutor = backgroundExecutor;
    mBackgroundExecutorServiceOwnedByThisController =
        backgroundExecutorServiceOwnedByThisController;
    mCallbackFromBackgroundExecutor = callbackFromBackgroundExecutor;
  }

  SyncNowController(TotpClock totpClock, NetworkTimeProvider networkTimeProvider) {
    this(
        totpClock,
        networkTimeProvider,
        Executors.newSingleThreadExecutor(),
        true,
        new RunOnThisLooperThreadExecutor());
  }

  void attach(Presenter presenter) {
    mPresenter = presenter;
    switch (mState) {
      case NOT_STARTED:
        start();
        break;
      case IN_PROGRESS:
        if (mPresenter != null) {
          mPresenter.onStarted();
        }
        break;
      case DONE:
        if (mPresenter != null) {
          mPresenter.onDone(mResult);
        }
        break;
      default:
        throw new IllegalStateException(String.valueOf(mState));
    }
  }


  void detach(Presenter presenter) {
    if (presenter != mPresenter) {
      return;
    }
    switch (mState) {
      case NOT_STARTED:
      case IN_PROGRESS:
        onCancelledByUser();
        break;
      case DONE:
        break;
      default:
        throw new IllegalStateException(String.valueOf(mState));
    }
  }


  void abort(Presenter presenter) {
    if (mPresenter != presenter) {
      return;
    }
    onCancelledByUser();
  }

  /**
   * Starts this controller's operation (initiates a Time Sync).
   */
  private void start() {
    mState = State.IN_PROGRESS;
    if (mPresenter != null) {
      mPresenter.onStarted();
    }
    // Avoid blocking this thread on the Time Sync operation by invoking it on a different thread
    // (provided by the Executor) and posting the results back to this thread.
    mBackgroundExecutor.execute(new Runnable() {
      @Override
      public void run() {
        runBackgroundSyncAndPostResult(mCallbackFromBackgroundExecutor);
      }
    });
  }

  private void onCancelledByUser() {
    finish(Result.CANCELLED_BY_USER);
  }


  private void onNewTimeCorrectionObtained(int timeCorrectionMinutes) {
    if (mState != State.IN_PROGRESS) {
      // Don't apply the new time correction if this controller is not waiting for this.
      // This callback may be invoked after the Time Sync operation has been cancelled or stopped
      // prematurely.
      return;
    }

    long oldTimeCorrectionMinutes = mTotpClock.getTimeCorrectionMinutes();
    Log.i(LOG_TAG, "Obtained new time correction: "
        + timeCorrectionMinutes + " min, old time correction: "
        + oldTimeCorrectionMinutes + " min");
    if (timeCorrectionMinutes == oldTimeCorrectionMinutes) {
      finish(Result.TIME_ALREADY_CORRECT);
    } else {
      mTotpClock.setTimeCorrectionMinutes(timeCorrectionMinutes);
      finish(Result.TIME_CORRECTED);
    }
  }


  private void finish(Result result) {
    if (mState == State.DONE) {
      // Not permitted to change state when already DONE
      return;
    }
    if (mBackgroundExecutorServiceOwnedByThisController) {
      ((ExecutorService) mBackgroundExecutor).shutdownNow();
    }
    mState = State.DONE;
    mResult = result;
    if (mPresenter != null) {
      mPresenter.onDone(result);
    }
  }


  private void runBackgroundSyncAndPostResult(Executor callbackExecutor) {
    long networkTimeMillis;
    try {
      networkTimeMillis = mNetworkTimeProvider.getNetworkTime();
    } catch (IOException e) {
      Log.w(LOG_TAG, "Failed to obtain network time due to connectivity issues");
      callbackExecutor.execute(new Runnable() {
        @Override
        public void run() {
          finish(Result.ERROR_CONNECTIVITY_ISSUE);
        }
      });
      return;
    }

    long timeCorrectionMillis = networkTimeMillis - System.currentTimeMillis();
    final int timeCorrectionMinutes = (int) Math.round(
        ((double) timeCorrectionMillis) / Utilities.MINUTE_IN_MILLIS);
    callbackExecutor.execute(new Runnable() {
      @Override
      public void run() {
        onNewTimeCorrectionObtained(timeCorrectionMinutes);
      }
    });
  }
}
