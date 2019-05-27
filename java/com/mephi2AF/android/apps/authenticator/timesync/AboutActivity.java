
package com.mephi2AF.android.apps.authenticator.timesync;

import com.mephi2AF.android.apps.authenticator.wizard.WizardPageActivity;
import com.mephi2AF.android.apps.authenticator2.R;

import android.os.Bundle;

import java.io.Serializable;

 class AboutActivity extends WizardPageActivity<Serializable> {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setPageContentView(R.layout.timesync_about);
    setTextViewHtmlFromResource(R.id.details, R.string.timesync_about_feature_screen_details);

    setButtonBarModeMiddleButtonOnly();
    mMiddleButton.setText(R.string.ok);
  }

  @Override
  protected void onMiddleButtonPressed() {
    onBackPressed();
  }
}
