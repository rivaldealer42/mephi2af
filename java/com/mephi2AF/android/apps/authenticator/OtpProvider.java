
package com.mephi2AF.android.apps.authenticator;

import com.mephi2AF.android.apps.authenticator.AccountDb.OtpType;
import com.mephi2AF.android.apps.authenticator.PasscodeGenerator.Signer;
import com.mephi2AF.android.apps.authenticator.AccountDb;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Collection;


public class OtpProvider implements com.mephi2AF.android.apps.authenticator.OtpSource {

  private static final int PIN_LENGTH = 6; // HOTP or TOTP
  private static final int REFLECTIVE_PIN_LENGTH = 9; // ROTP

  @Override
  public int enumerateAccounts(Collection<String> result) {
    return mAccountDb.getNames(result);
  }

  @Override
  public String getNextCode(String accountName) throws com.mephi2AF.android.apps.authenticator.OtpSourceException {
    return getCurrentCode(accountName, null);
  }

  // This variant is used when an additional challenge, such as URL or
  // transaction details, are included in the OTP request.
  // The additional string is appended to standard HOTP/TOTP state before
  // applying the MAC function.
  @Override
  public String respondToChallenge(String accountName, String challenge) throws com.mephi2AF.android.apps.authenticator.OtpSourceException {
    if (challenge == null) {
      return getCurrentCode(accountName, null);
    }
    try {
      byte[] challengeBytes = challenge.getBytes("UTF-8");
      return getCurrentCode(accountName, challengeBytes);
    } catch (UnsupportedEncodingException e) {
      return "";
    }
  }

  @Override
  public com.mephi2AF.android.apps.authenticator.TotpCounter getTotpCounter() {
    return mTotpCounter;
  }

  @Override
  public com.mephi2AF.android.apps.authenticator.TotpClock getTotpClock() {
    return mTotpClock;
  }

  private String getCurrentCode(String username, byte[] challenge) throws com.mephi2AF.android.apps.authenticator.OtpSourceException {
    // Account name is required.
    if (username == null) {
      throw new com.mephi2AF.android.apps.authenticator.OtpSourceException("No account name");
    }

    OtpType type = mAccountDb.getType(username);
    String secret = getSecret(username);

    long otp_state = 0;

    if (type == OtpType.TOTP) {
      // For time-based OTP, the state is derived from clock.
      otp_state =
          mTotpCounter.getValueAtTime(com.mephi2AF.android.apps.authenticator.Utilities.millisToSeconds(mTotpClock.currentTimeMillis()));
    } else if (type == OtpType.HOTP){
      // For counter-based OTP, the state is obtained by incrementing stored counter.
      mAccountDb.incrementCounter(username);
      Integer counter = mAccountDb.getCounter(username);
      otp_state = counter.longValue();
    }

    return computePin(secret, otp_state, challenge);
  }

  public OtpProvider(AccountDb accountDb, com.mephi2AF.android.apps.authenticator.TotpClock totpClock) {
    this(DEFAULT_INTERVAL, accountDb, totpClock);
  }

  public OtpProvider(int interval, AccountDb accountDb, com.mephi2AF.android.apps.authenticator.TotpClock totpClock) {
    mAccountDb = accountDb;
    mTotpCounter = new com.mephi2AF.android.apps.authenticator.TotpCounter(interval);
    mTotpClock = totpClock;
  }

  /**
   * Computes the one-time PIN given the secret key.
   *
   * @param secret the secret key
   * @param otp_state current token state (counter or time-interval)
   * @param challenge optional challenge bytes to include when computing passcode.
   * @return the PIN
   */
  private String computePin(String secret, long otp_state, byte[] challenge)
      throws com.mephi2AF.android.apps.authenticator.OtpSourceException {
    if (secret == null || secret.length() == 0) {
      throw new com.mephi2AF.android.apps.authenticator.OtpSourceException("Null or empty secret");
    }

    try {
      Signer signer = AccountDb.getSigningOracle(secret);
      com.mephi2AF.android.apps.authenticator.PasscodeGenerator pcg = new com.google.android.apps.authenticator.PasscodeGenerator(signer,
        (challenge == null) ? PIN_LENGTH : REFLECTIVE_PIN_LENGTH);

      return (challenge == null) ?
             pcg.generateResponseCode(otp_state) :
             pcg.generateResponseCode(otp_state, challenge);
    } catch (GeneralSecurityException e) {
      throw new com.mephi2AF.android.apps.authenticator.OtpSourceException("Crypto failure", e);
    }
  }

  /**
   * Reads the secret key that was saved on the phone.
   * @param user Account name identifying the user.
   * @return the secret key as base32 encoded string.
   */
  String getSecret(String user) {
    return mAccountDb.getSecret(user);
  }

  /** Default passcode timeout period (in seconds) */
  public static final int DEFAULT_INTERVAL = 30;

  private final AccountDb mAccountDb;

  /** Counter for time-based OTPs (TOTP). */
  private final com.mephi2AF.android.apps.authenticator.TotpCounter mTotpCounter;

  /** Clock input for time-based OTPs (TOTP). */
  private final com.mephi2AF.android.apps.authenticator.TotpClock mTotpClock;
}
