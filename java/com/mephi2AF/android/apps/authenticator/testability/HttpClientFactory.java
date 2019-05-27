

package com.mephi2AF.android.apps.authenticator.testability;

import android.content.Context;
import android.os.Build;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


final class HttpClientFactory {

  /** Timeout (ms) for establishing a connection.*/
  // @VisibleForTesting
  static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 20 * 1000;

  /** Timeout (ms) for read operations on connections. */
  // @VisibleForTesting
  static final int DEFAULT_READ_TIMEOUT_MILLIS = 20 * 1000;

  /** Timeout (ms) for obtaining a connection from the connection pool.*/
  // @VisibleForTesting
  static final int DEFAULT_GET_CONNECTION_FROM_POOL_TIMEOUT_MILLIS = 20 * 1000;

  /** Hidden constructor to prevent instantiation. */
  private HttpClientFactory() {}

  /**
   * Creates a new {@link HttpClient}.
   *
   * @param context context for reusing SSL sessions.
   */
  static HttpClient createHttpClient(Context context) {
    HttpClient client;

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
      try {
        client = createHttpClientForFroyoAndHigher(context);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create HttpClient", e);
      }
    } else {
      client = createHttpClientForEclair();
    }

    configureHttpClient(client);
    return client;
  }

  private static void configureHttpClient(HttpClient httpClient) {
    HttpParams params = httpClient.getParams();
    HttpConnectionParams.setStaleCheckingEnabled(params, false);
    HttpConnectionParams.setConnectionTimeout(params, DEFAULT_CONNECT_TIMEOUT_MILLIS);
    HttpConnectionParams.setSoTimeout(params, DEFAULT_READ_TIMEOUT_MILLIS);
    HttpConnectionParams.setSocketBufferSize(params, 8192);
    ConnManagerParams.setTimeout(params, DEFAULT_GET_CONNECTION_FROM_POOL_TIMEOUT_MILLIS);

    // Don't handle redirects automatically
    HttpClientParams.setRedirecting(params, false);

    // Don't handle authentication automatically
    HttpClientParams.setAuthenticating(params, false);
  }

  private static HttpClient createHttpClientForFroyoAndHigher(Context context)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
          InvocationTargetException {
    // IMPLEMENTATION NOTE: We create the instance via Reflection API to avoid raising the
    // target SDK version to 8 because that will cause Eclipse to complain for no good reason.
    // The code below invokes:
    //   AndroidHttpClient.newInstance(null, getContext())
    Class<?> androidHttpClientClass =
        context.getClassLoader().loadClass("android.net.http.AndroidHttpClient");
    Method newInstanceMethod =
        androidHttpClientClass.getMethod("newInstance", String.class, Context.class);
    return (HttpClient) newInstanceMethod.invoke(null, null, context);
  }


  private static HttpClient createHttpClientForEclair() {
    // IMPLEMENTATION NOTE: Since AndroidHttpClient is not available on Eclair, we create a
    // DefaultHttpClient with a configuration similar to that of AndroidHttpClient.
    return new DefaultHttpClient(new BasicHttpParams());
  }
}
