package com.gerrit;


import java.io.IOException;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http post请求.
 *
 * @author 王春亮
 * @date 2019-04-15 11:27
 */
public class HttpClientUtil {

  private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

  private static final int SUCCESS_CODE = 200;

  public static String sendPost(String url, String json, String token) {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setHeader("X-Gitlab-Event", "Push Hook");
    httpPost.setHeader("X-Gitlab-Token", token);

    // 我们可以使用一个Builder来设置UA字段，然后再创建HttpClient对象
    HttpClientBuilder builder = HttpClients.custom();
    // 对照UA字串的标准格式理解一下每部分的意思
    builder.setUserAgent("Mozilla/5.0(Windows;U;Windows NT 5.1;en-US;rv:0.9.4)");
    final CloseableHttpClient client = builder.build();

    String respContent = null;

    StringEntity entity = new StringEntity(json, "utf-8");
    entity.setContentEncoding("UTF-8");
    entity.setContentType("application/json");
    httpPost.setEntity(entity);

    CloseableHttpResponse resp = null;
    try {
      resp = client.execute(httpPost);
      System.out.println(resp);
      if (resp.getStatusLine().getStatusCode() == SUCCESS_CODE) {
        HttpEntity re = resp.getEntity();
        respContent = EntityUtils.toString(re, "UTF-8");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      closeClientAndResp(client, resp);
    }
    return respContent;
  }

  public static String newGet(String url) {
    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;

    try {
      HttpClientBuilder builder = HttpClients.custom();
      // 对照UA字串的标准格式理解一下每部分的意思
      builder.setUserAgent("Mozilla/5.0(Windows;U;Windows NT 5.1;en-US;rv:0.9.4)");
      client = builder.build();
      URIBuilder uriBuilder = new URIBuilder(url);
      HttpGet httpGet = new HttpGet(uriBuilder.build());

      httpGet.addHeader("Accept", "application/json;charset=utf-8");
      httpGet.addHeader("Upgrade-Insecure-Requests", "1");


      response = client.execute(httpGet);

      int statusCode = response.getStatusLine().getStatusCode();
      if (SUCCESS_CODE == statusCode) {
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        System.out.println(result);
        return result;
      }
    } catch (Exception e) {
      logger.error("httpclient no success, the exception is {}.", e.getMessage());
    } finally {
      closeClientAndResp(client, response);
    }
    return null;
  }


  public static String sendGet(String url, String base64Auth, List<NameValuePair> nameValuePairs) {
    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;

    try {
      HttpClientBuilder builder = HttpClients.custom();
      // 对照UA字串的标准格式理解一下每部分的意思
      builder.setUserAgent("Mozilla/5.0(Windows;U;Windows NT 5.1;en-US;rv:0.9.4)");
      client = builder.build();
      URIBuilder uriBuilder = new URIBuilder(url);
      if (nameValuePairs != null && !nameValuePairs.isEmpty()) {
        uriBuilder.addParameters(nameValuePairs);
      }
      HttpGet httpGet = new HttpGet(uriBuilder.build());

      httpGet.addHeader("Authorization", base64Auth);
      httpGet.addHeader("Accept", "application/json;charset=utf-8");
      httpGet.addHeader("Upgrade-Insecure-Requests", "1");


      response = client.execute(httpGet);

      int statusCode = response.getStatusLine().getStatusCode();
      if (SUCCESS_CODE == statusCode) {
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");

        return result;
      }
    } catch (Exception e) {
      logger.error("httpclient no success, the exception is {}.", e.getMessage());
    } finally {
      closeClientAndResp(client, response);
    }
    return null;
  }



  private static void closeClientAndResp(
          CloseableHttpClient client, CloseableHttpResponse response) {
    try {
      response.close();
    } catch (IOException e) {
      logger.error("close response exception is {}.", e.getMessage());
    }
    try {
      client.close();
    } catch (IOException e) {
      logger.error("close client exception is {}.", e.getMessage());
    }
  }
}
