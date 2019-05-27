
package com.mephi2AF.android.apps.authenticator;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.mephi2AF.android.apps.authenticator.AccountDb;
import com.mephi2AF.android.apps.authenticator.AuthenticatorActivity;


public interface OptionalFeatures {


  void onAuthenticatorActivityCreated(AuthenticatorActivity activity);


  void onAuthenticatorActivityAccountSaved(Context context, String account);


  com.mephi2AF.android.apps.authenticator.OtpSource createOtpSource(AccountDb accountDb, com.google.android.apps.authenticator.TotpClock totpClock);


  void onAuthenticatorActivityGetNextOtpFailed(
      AuthenticatorActivity activity, String accountName, com.mephi2AF.android.apps.authenticator.OtpSourceException exception);


  Dialog onAuthenticatorActivityCreateDialog(AuthenticatorActivity activity, int id);


  void onAuthenticatorActivityAddAccount(AuthenticatorActivity activity);


  boolean interpretScanResult(Context context, Uri scanResult);


  void onDataImportedFromOldApp(Context context);


  SharedPreferences getSharedPreferencesForDataImportFromOldApp(Context context);


  String appendDataImportLearnMoreLink(Context context, String text);

}
