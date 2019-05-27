
package com.mephi2AF.android.apps.authenticator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mephi2AF.android.apps.authenticator.Base32String.DecodingException;
import com.mephi2AF.android.apps.authenticator.testability.DependencyInjector;
import com.mephi2AF.android.apps.authenticator2.R;
import com.mephi2AF.android.apps.authenticator.AccountDb;
import com.mephi2AF.android.apps.authenticator.Base32String;
import org.bouncycastle.jcajce.provider.digest.BCMessageDigest;
import org.bouncycastle.jcajce.provider.digest;
import java.lang.Object;
import java.security.MessageDigestSpi;
import java.security.MessageDigest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class CheckCodeActivity extends Activity {
  private TextView mCheckCodeTextView;
  private TextView mCodeTextView;
  private TextView mCounterValue;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.check_code);
    mCodeTextView = (TextView) findViewById(R.id.code_value);
    mCheckCodeTextView = (TextView) findViewById(R.id.check_code);
    mCounterValue = (TextView) findViewById(R.id.counter_value);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    String user = extras.getString("user");

    AccountDb accountDb = DependencyInjector.getAccountDb();
    AccountDb.OtpType type = accountDb.getType(user);
    if (type == AccountDb.OtpType.HOTP) {
      mCounterValue.setText(accountDb.getCounter(user).toString());
      findViewById(R.id.counter_area).setVisibility(View.VISIBLE);
    } else {
      findViewById(R.id.counter_area).setVisibility(View.GONE);
    }

    String secret = accountDb.getSecret(user);
    String checkCode = null;
    String errorMessage = null;
    try {
      checkCode = getCheckCode(secret);
    } catch (GeneralSecurityException e) {
      errorMessage = getString(R.string.general_security_exception);
    } catch (DecodingException e) {
      errorMessage = getString(R.string.decoding_exception);
    }
    if (errorMessage != null) {
      mCheckCodeTextView.setText(errorMessage);
      return;
    }
    mCodeTextView.setText(checkCode);
    String checkCodeMessage = String.format(getString(R.string.check_code),
        TextUtils.htmlEncode(user));
    CharSequence styledCheckCode = Html.fromHtml(checkCodeMessage);
    mCheckCodeTextView.setText(styledCheckCode);
    mCheckCodeTextView.setVisibility(View.VISIBLE);
    findViewById(R.id.code_area).setVisibility(View.VISIBLE);
  }
  static String getCheckCode(String secret) throws GeneralSecurityException,
      DecodingException {
    final byte[] keyBytes = Base32String.decode(secret);
    HMac mac = new HMac(new GOST3411.Digest());
    mac.init(new KeyParameter(keyBytes));
    com.mephi2AF.android.apps.authenticator.PasscodeGenerator pcg = new com.mephi2AF.android.apps.authenticator.PasscodeGenerator(mac);
    return pcg.generateResponseCode(0L);
  }

}
