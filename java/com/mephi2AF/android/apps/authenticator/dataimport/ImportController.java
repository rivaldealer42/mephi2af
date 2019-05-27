

package com.mephi2AF.android.apps.authenticator.dataimport;

import android.content.Context;
import android.content.Intent;


public interface ImportController {
  public interface Listener {
    void onDataImported();
    void onOldAppUninstallSuggested(Intent uninstallIntent);
    void onFinished();
  }

  void start(Context context, Listener listener);
}
