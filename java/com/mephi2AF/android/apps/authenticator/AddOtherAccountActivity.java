
package com.mephi2AF.android.apps.authenticator;

import com.mephi2AF.android.apps.authenticator.wizard.WizardPageActivity;
import com.mephi2AF.android.apps.authenticator.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;


public class AddOtherAccountActivity extends WizardPageActivity<Serializable> {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setPageContentView(R.layout.add_other_account);

    findViewById(R.id.manually_add_account).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        manuallyEnterAccountDetails();
      }
    });

    mRightButton.setVisibility(View.INVISIBLE);
  }

  private void manuallyEnterAccountDetails() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setClass(this, com.mephi2AF.android.apps.authenticator.EnterKeyActivity.class);
    startActivity(intent);
  }

}
