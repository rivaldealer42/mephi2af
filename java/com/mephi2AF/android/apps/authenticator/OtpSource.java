

package com.mephi2AF.android.apps.authenticator;

import java.util.Collection;


public interface OtpSource {


  int enumerateAccounts(Collection<String> result);


  String getNextCode(String accountName) throws com.mephi2AF.android.apps.authenticator.OtpSourceException;

  /**
   * Generate response to a given challenge based on next OTP code.
   * Subclasses are not required to implement this method.
   *
   * @param accountName Username, email address or other unique identifier for the account.
   * @param challenge Server specified challenge as UTF8 string.
   * @return Response to the challenge.
   * @throws UnsupportedOperationException if the token does not support
   *         challenge-response extension for this account.
   */
  String respondToChallenge(String accountName, String challenge) throws com.mephi2AF.android.apps.authenticator.OtpSourceException;

  /**
   * Gets the counter for generating or verifying TOTP codes.
   */
  com.mephi2AF.android.apps.authenticator.TotpCounter getTotpCounter();

  /**
   * Gets the clock for generating or verifying TOTP codes.
   */
  com.mephi2AF.android.apps.authenticator.TotpClock getTotpClock();
}
