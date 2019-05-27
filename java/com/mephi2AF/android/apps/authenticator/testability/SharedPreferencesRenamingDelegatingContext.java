
package com.mephi2AF.android.apps.authenticator.testability;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;


public class SharedPreferencesRenamingDelegatingContext extends ContextWrapper {

  private final String mPrefix;

  public SharedPreferencesRenamingDelegatingContext(Context delegate, String prefix) {
    super(delegate);
    mPrefix = prefix;
  }

  @Override
  public SharedPreferences getSharedPreferences(String name, int mode) {
    return super.getSharedPreferences(mPrefix + name, mode);
  }
}
