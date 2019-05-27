
package com.mephi2AF.android.apps.authenticator;

import com.google.android.apps.authenticator2.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TextView;


public class UserRowView extends LinearLayout {

  public UserRowView(Context context) {
    super(context);
  }

  public UserRowView(Context context, AttributeSet attrset) {
    super(context, attrset);
  }

  @Override
  public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessEvent) {
    Context ctx = this.getContext();

    String message = "";
    CharSequence pinText = ((TextView) findViewById(R.id.pin_value)).getText();
    if (ctx.getString(R.string.empty_pin).equals(pinText)){
        message = ctx.getString(R.string.counter_pin);
    } else {
        for (int i = 0; i < pinText.length(); i++) {
            message = message + pinText.charAt(i) + " ";
        }
    }
    CharSequence userText = ((TextView) findViewById(R.id.current_user)).getText();
    message = message + " " + userText;
    accessEvent.setClassName(getClass().getName());
    accessEvent.setPackageName(ctx.getPackageName());
    accessEvent.getText().add(message);

    return true;
  }
}
