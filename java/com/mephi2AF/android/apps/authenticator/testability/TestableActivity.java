
package com.mephi2AF.android.apps.authenticator.testability;

import android.app.Activity;
import android.content.Intent;

import com.mephi2AF.android.apps.authenticator.testability.DependencyInjector;
import com.mephi2AF.android.apps.authenticator.testability.StartActivityListener;


public class TestableActivity extends Activity {

  @Override
  public void startActivity(Intent intent) {
    StartActivityListener listener = DependencyInjector.getStartActivityListener();
    if ((listener != null) && (listener.onStartActivityInvoked(this, intent))) {
      return;
    }

    super.startActivity(intent);
  }

  @Override
  public void startActivityForResult(Intent intent, int requestCode) {
    StartActivityListener listener = DependencyInjector.getStartActivityListener();
    if ((listener != null) && (listener.onStartActivityInvoked(this, intent))) {
      return;
    }

    super.startActivityForResult(intent, requestCode);
  }
}
