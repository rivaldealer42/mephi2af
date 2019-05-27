

package com.mephi2AF.android.apps.authenticator;

import com.mephi2AF.android.apps.authenticator.testability.DependencyInjector;

import android.app.Application;


public class AuthenticatorApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();


    try {
      com.mephi2AF.android.apps.authenticator.FileUtilities.restrictAccessToOwnerOnly(
          getApplicationContext().getApplicationInfo().dataDir);
    } catch (Throwable e) {

    }


    DependencyInjector.configureForProductionIfNotConfigured(getApplicationContext());
  }

  @Override
  public void onTerminate() {
    DependencyInjector.close();

    super.onTerminate();
  }
}
