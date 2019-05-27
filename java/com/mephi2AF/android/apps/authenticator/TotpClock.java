
package com.mephi2AF.android.apps.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class TotpClock implements SharedPreferences.OnSharedPreferenceChangeListener {

  // @VisibleForTesting
  static final String PREFERENCE_KEY_OFFSET_MINUTES = "timeCorrectionMinutes";

  private final SharedPreferences mPreferences;

  private final Object mLock = new Object();


  private Integer mCachedCorrectionMinutes;

  public TotpClock(Context context) {
    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    mPreferences.registerOnSharedPreferenceChangeListener(this);
  }


  public long currentTimeMillis() {
    return System.currentTimeMillis() + getTimeCorrectionMinutes() * com.mephi2AF.android.apps.authenticator.Utilities.MINUTE_IN_MILLIS;
  }


  public int getTimeCorrectionMinutes() {
    synchronized (mLock) {
      if (mCachedCorrectionMinutes == null) {
        try {
           mCachedCorrectionMinutes = mPreferences.getInt(PREFERENCE_KEY_OFFSET_MINUTES, 0);
        } catch(ClassCastException e) {
           mCachedCorrectionMinutes = Integer.valueOf(mPreferences.getString(PREFERENCE_KEY_OFFSET_MINUTES, "0"));
        }
      }
      return mCachedCorrectionMinutes;
    }
  }


  public void setTimeCorrectionMinutes(int minutes) {
    synchronized (mLock) {
      mPreferences.edit().putInt(PREFERENCE_KEY_OFFSET_MINUTES, minutes).commit();
      // Invalidate the cache to force reading actual settings from time to time
      mCachedCorrectionMinutes = null;
    }
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(PREFERENCE_KEY_OFFSET_MINUTES)) {
      // Invalidate the cache
      mCachedCorrectionMinutes = null;
    }
  }
}
