
package com.mephi2AF.android.apps.authenticator.timesync;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.IOException;
import java.util.Date;


public class NetworkTimeProvider {

  private static final String LOG_TAG = NetworkTimeProvider.class.getSimpleName();
  private static final String URL = "https://www.google.com";

  private final HttpClient mHttpClient;

  public NetworkTimeProvider(HttpClient httpClient) {
    mHttpClient = httpClient;
  }


  public long getNetworkTime() throws IOException {
    HttpHead request = new HttpHead(URL);
    Log.i(LOG_TAG, "Sending request to " + request.getURI());
    HttpResponse httpResponse;
    try {
      httpResponse = mHttpClient.execute(request);
    } catch (ClientProtocolException e) {
      throw new IOException(String.valueOf(e));
    } catch (IOException e) {
      throw new IOException("Failed due to connectivity issues: " + e);
    }

    try {
      Header dateHeader = httpResponse.getLastHeader("Date");
      Log.i(LOG_TAG, "Received response with Date header: " + dateHeader);
      if (dateHeader == null) {
        throw new IOException("No Date header in response");
      }
      String dateHeaderValue = dateHeader.getValue();
      try {
        Date networkDate = DateUtils.parseDate(dateHeaderValue);
        return networkDate.getTime();
      } catch (DateParseException e) {
        throw new IOException(
            "Invalid Date header format in response: \"" + dateHeaderValue + "\"");
      }
    } finally {
      // Consume all of the content of the response to facilitate HTTP 1.1 persistent connection
      // reuse and to avoid running out of connections when this methods is scheduled on different
      // threads.
      try {
        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null) {
          responseEntity.consumeContent();
        }
      } catch (IOException e) {
        // Ignored because this is not an error that is relevant to clients of this transport.
      }
    }
  }
}
