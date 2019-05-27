
package com.mephi2AF.android.apps.authenticator.timesync;

import com.mephi2AF.android.apps.authenticator.testability.DependencyInjector;
import com.mephi2AF.android.apps.authenticator2.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;


public class SyncNowActivity extends Activity implements SyncNowController.Presenter {


  private SyncNowController mController;

  private Dialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getLastNonConfigurationInstance() != null) {
      mController = (SyncNowController) getLastNonConfigurationInstance();
    } else {
      mController = new SyncNowController(
          DependencyInjector.getTotpClock(),
          new NetworkTimeProvider(DependencyInjector.getHttpClient()));
    }

    mController.attach(this);
  }

  @Override
  protected void onStop() {
    if (isFinishing()) {
      mController.detach(this);
    }
    super.onStop();
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    return mController;
  }

  @Override
  public void onBackPressed() {
    mController.abort(this);
  }

  // --------- SyncNowController.Presenter interface implementation ------

  @Override
  public void onStarted() {
    if (isFinishing()) {
      // Ignore this callback if this Activity is already finishing or is already finished
      return;
    }

    showInProgressDialog();
  }

  @Override
  public void onDone(SyncNowController.Result result) {
    if (isFinishing()) {
      // Ignore this callback if this Activity is already finishing or is already finished
      return;
    }

    dismissInProgressDialog();

    switch (result) {
      case TIME_ALREADY_CORRECT:
        new AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.timesync_sync_now_time_already_correct_dialog_title)
            .setMessage(R.string.timesync_sync_now_time_already_correct_dialog_details)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            })
            .create()
            .show();
        break;
      case TIME_CORRECTED:
        new AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.timesync_sync_now_time_corrected_dialog_title)
            .setMessage(R.string.timesync_sync_now_time_corrected_dialog_details)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            })
            .create()
            .show();
        break;
      case ERROR_CONNECTIVITY_ISSUE:
        new AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.timesync_sync_now_connectivity_error_dialog_title)
            .setMessage(R.string.timesync_sync_now_connectivity_error_dialog_details)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            })
            .create()
            .show();
        break;
      case CANCELLED_BY_USER:
        finish();
        break;
      default:
        throw new IllegalArgumentException(String.valueOf(result));
    }
  }



  private void showInProgressDialog() {
    mProgressDialog = ProgressDialog.show(
        this,
        getString(R.string.timesync_sync_now_progress_dialog_title),
        getString(R.string.timesync_sync_now_progress_dialog_details),
        true,
        true);
    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        mController.abort(SyncNowActivity.this);
      }
    });
  }


  private void dismissInProgressDialog() {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }
  }
}
