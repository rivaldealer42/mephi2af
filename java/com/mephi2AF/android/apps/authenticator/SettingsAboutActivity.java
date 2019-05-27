

package com.mephi2AF.android.apps.authenticator;

import com.mephi2AF.android.apps.authenticator.testability.TestablePreferenceActivity;
import com.mephi2AF.android.apps.authenticator2.R;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;


public class SettingsAboutActivity extends TestablePreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences_about);

    String packageVersion = "";
    try {
      packageVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {}
    findPreference("version").setSummary(packageVersion);
  }
}
