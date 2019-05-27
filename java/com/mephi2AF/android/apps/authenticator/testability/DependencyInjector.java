
package com.mephi2AF.android.apps.authenticator.testability;

import com.mephi2AF.android.apps.authenticator.AccountDb;
import com.mephi2AF.android.apps.authenticator.AuthenticatorActivity;
import com.mephi2AF.android.apps.authenticator.MarketBuildOptionalFeatures;
import com.mephi2AF.android.apps.authenticator.OptionalFeatures;
import com.mephi2AF.android.apps.authenticator.OtpSource;
import com.mephi2AF.android.apps.authenticator.TotpClock;
import com.mephi2AF.android.apps.authenticator.dataimport.ExportServiceBasedImportController;
import com.mephi2AF.android.apps.authenticator.dataimport.ImportController;

import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.test.RenamingDelegatingContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;



public final class DependencyInjector {

  private static Context sContext;

  private static AccountDb sAccountDb;
  private static OtpSource sOtpProvider;
  private static TotpClock sTotpClock;
  private static PackageManager sPackageManager;
  private static com.mephi2AF.android.apps.authenticator.testability.StartActivityListener sStartActivityListener;
  private static HttpClient sHttpClient;
  private static ImportController sImportController;
  private static OptionalFeatures sOptionalFeatures;

  private enum Mode {
    PRODUCTION,
    INTEGRATION_TEST,
  }

  private static Mode sMode;

  private DependencyInjector() {}

  /**
   * Gets the {@link Context} passed the instances created by this injector.
   */
  public static synchronized Context getContext() {
    if (sContext == null) {
      throw new IllegalStateException("Context not set");
    }
    return sContext;
  }

  /**
   * Sets the {@link AccountDb} instance returned by this injector. This will prevent the injector
   * from creating its own instance.
   */
  public static synchronized void setAccountDb(AccountDb accountDb) {
    if (sAccountDb != null) {
      sAccountDb.close();
    }
    sAccountDb = accountDb;
  }

  public static synchronized AccountDb getAccountDb() {
    if (sAccountDb == null) {
      sAccountDb = new AccountDb(getContext());
      if (sMode != Mode.PRODUCTION) {
        sAccountDb.deleteAllData();
      }
    }
    return sAccountDb;
  }

  /**
   * Sets the {@link OtpSource} instance returned by this injector. This will prevent the injector
   * from creating its own instance.
   */
  public static synchronized void setOtpProvider(OtpSource otpProvider) {
    sOtpProvider = otpProvider;
  }

  public static synchronized OtpSource getOtpProvider() {
    if (sOtpProvider == null) {
      sOtpProvider = getOptionalFeatures().createOtpSource(getAccountDb(), getTotpClock());
    }
    return sOtpProvider;
  }

  /**
   * Sets the {@link TotpClock} instance returned by this injector. This will prevent the injector
   * from creating its own instance.
   */
  public static synchronized void setTotpClock(TotpClock totpClock) {
    sTotpClock = totpClock;
  }

  public static synchronized TotpClock getTotpClock() {
    if (sTotpClock == null) {
      sTotpClock = new TotpClock(getContext());
    }
    return sTotpClock;
  }

  /**
   * Sets the {@link PackageManager} instance returned by this injector. This will prevent the
   * injector from creating its own instance.
   */
  public static synchronized void setPackageManager(PackageManager packageManager) {
    sPackageManager = packageManager;
  }

  public static synchronized PackageManager getPackageManager() {
    if (sPackageManager == null) {
      sPackageManager = getContext().getPackageManager();
    }
    return sPackageManager;
  }

  /**
   * Sets the {@link StartActivityListener} instance returned by this injector.
   */
  public static synchronized void setStartActivityListener(com.mephi2AF.android.apps.authenticator.testability.StartActivityListener listener) {
    sStartActivityListener = listener;
  }

  public static synchronized com.mephi2AF.android.apps.authenticator.testability.StartActivityListener getStartActivityListener() {
    // Don't create an instance on demand -- the default behavior when the listener is null is to
    // proceed with launching activities.
    return sStartActivityListener;
  }

  /**
   * Sets the {@link ImportController} instance returned by this injector. This will prevent the
   * injector from creating its own instance.
   */
  public static synchronized void setDataImportController(ImportController importController) {
    sImportController = importController;
  }

  public static synchronized ImportController getDataImportController() {
    if (sImportController == null) {
      if (sMode == Mode.PRODUCTION) {
        sImportController = new ExportServiceBasedImportController();
      } else {
        // By default, use a no-op controller during tests to avoid them being dependent on the
        // presence of the "old" app on the device under test.
        sImportController = new ImportController() {
          @Override
          public void start(Context context, Listener listener) {}
        };
      }
    }
    return sImportController;
  }

  /**
   * Sets the {@link HttpClient} instance returned by this injector. This will prevent the
   * injector from creating its own instance.
   */
  public static synchronized void setHttpClient(HttpClient httpClient) {
    sHttpClient = httpClient;
  }

  public static synchronized HttpClient getHttpClient() {
    if (sHttpClient == null) {
      sHttpClient = com.mephi2AF.android.apps.authenticator.testability.HttpClientFactory.createHttpClient(getContext());
    }
    return sHttpClient;
  }

  public static synchronized void setOptionalFeatures(OptionalFeatures optionalFeatures) {
    sOptionalFeatures = optionalFeatures;
  }

  public static synchronized OptionalFeatures getOptionalFeatures() {
    if (sOptionalFeatures == null) {
      try {
        Class<?> resultClass = Class.forName(
            AuthenticatorActivity.class.getPackage().getName() + ".NonMarketBuildOptionalFeatures");
        try {
          sOptionalFeatures = (OptionalFeatures) resultClass.newInstance();
        } catch (Exception e) {
          throw new RuntimeException("Failed to instantiate optional features module", e);
        }
      } catch (ClassNotFoundException e) {
        sOptionalFeatures = new MarketBuildOptionalFeatures();
      }
    }
    return sOptionalFeatures;
  }

  /**
   * Clears any state and configures this injector for production use. Does nothing if the injector
   * is already configured.
   */
  public static synchronized void configureForProductionIfNotConfigured(Context context) {
    if (sMode != null) {
      return;
    }

    close();
    sMode = Mode.PRODUCTION;
    sContext = context;
  }

  /**
   * Clears any state and configures this injector to provide objects that are suitable for
   * integration testing.
   */
  // @VisibleForTesting
  public static synchronized void resetForIntegrationTesting(Context context) {
    if (context == null) {
      throw new NullPointerException("context == null");
    }

    close();

    sMode = Mode.INTEGRATION_TEST;
    RenamingDelegatingContext renamingContext = new RenamingDelegatingContext(context, "test_");
    renamingContext.makeExistingFilesAndDbsAccessible();
    sContext = new com.mephi2AF.android.apps.authenticator.testability.SharedPreferencesRenamingDelegatingContext(renamingContext, "test_");
    PreferenceManager.getDefaultSharedPreferences(sContext).edit().clear().commit();
  }

  /**
   * Closes any resources and objects held by this injector. To use the injector again, invoke
   * {@link #resetForIntegrationTesting(Context)}.
   */
  public static synchronized void close() {
    if (sAccountDb != null) {
      sAccountDb.close();
    }
    if (sHttpClient != null) {
      ClientConnectionManager httpClientConnectionManager = sHttpClient.getConnectionManager();
      if (httpClientConnectionManager != null) {
        httpClientConnectionManager.shutdown();
      }
    }

    sMode = null;
    sContext = null;
    sAccountDb = null;
    sOtpProvider = null;
    sTotpClock = null;
    sPackageManager = null;
    sStartActivityListener = null;
    sHttpClient = null;
    sImportController = null;
    sOptionalFeatures = null;
  }
}
