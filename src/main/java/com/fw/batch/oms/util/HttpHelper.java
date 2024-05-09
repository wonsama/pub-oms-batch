package com.fw.batch.oms.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * The HttpHelper class provides utility methods for making HTTP GET requests.
 */
@Slf4j
public class HttpHelper {

  /**
   * Sends an HTTP GET request to the specified path and returns the response as a
   * string.
   *
   * @param path the URL path to send the GET request to
   * @return the response message as a string
   */
  public static String requestGet(String path) {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(path);
    request.setHeader("Accept", "application/json");
    request.setHeader("Content-type", "application/json");

    log.info("called : {}", path);
    try {
      // Sends the request and receives the response message
      return client.execute(request, response -> {
        return EntityUtils.toString(response.getEntity(), "UTF-8");
      });
    } catch (IOException e) {
      // Handles the response message in case of an error
      Map<String, String> map = new HashMap<String, String>();
      Gson gson = new Gson();

      map.put("responseCode", "500");
      map.put("responseMessage", e.getMessage());

      return gson.toJson(map).toString();
    }
  }
}
