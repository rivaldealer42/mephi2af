

package com.mephi2AF.android.apps.authenticator;

import android.os.Handler;

import java.util.concurrent.Executor;


public class RunOnThisLooperThreadExecutor implements Executor {

  private final Handler mHandler = new Handler();

  @Override
  public void execute(Runnable command) {
    if (Thread.currentThread() == mHandler.getLooper().getThread()) {
      // The calling thread is the target thread of the Handler -- invoke immediately, blocking
      // the calling thread.
      command.run();
    } else {
      // The calling thread is not the same as the thread with which the Handler is associated --
      // post to the Handler for later execution.
      mHandler.post(command);
    }
  }
}
