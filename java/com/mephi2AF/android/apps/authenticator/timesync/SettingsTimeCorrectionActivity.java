
package com.mephi2AF.android.apps.authenticator.timesync;

import com.mephi2AF.android.apps.authenticator.testability.TestablePreferenceActivity;
import com.mephi2AF.android.apps.authenticator2.R;

import android.os.Bundle;


public class SettingsTimeCorrectionActivity extends TestablePreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences_time_correction);
  }
}
