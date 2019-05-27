
package com.mephi2AF.android.apps.authenticator.testability;

import android.content.Context;
import android.content.Intent;


public interface StartActivityListener {


  boolean onStartActivityInvoked(Context sourceContext, Intent intent);
}
