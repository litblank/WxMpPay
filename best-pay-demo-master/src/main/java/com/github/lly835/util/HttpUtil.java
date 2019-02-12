package com.github.lly835.util;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    /**
     *
     * @param url 请求地址
     * @param data 请求数据
     * @return String 响应string
     * @throws Exception 异常
     */
    public static String post(String url, Map<String,String> data) throws Exception{
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        if (data != null) {
            List<NameValuePair> nvps = new ArrayList<>();
            data.forEach((key, value) -> {
                nvps.add(new BasicNameValuePair(key, value));
            });
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        }
        ResponseHandler<String> responseHandler = getStringResponseHandler();
        return httpClient.execute(httpPost, responseHandler);
    }

    /**
     * @param url  请求地址
     * @param body 请求数据
     * @return responseContent 响应数据
     */
    public static String post(String url, String body) {
        String responseContent = "";
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");

            StringEntity s = new StringEntity(body, "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(s);

//            httpPost.setEntity(new StringEntity(body));

            response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, Consts.UTF_8);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseContent;
    }

    /**
     *
     * @param url 请求地址
     * @throws Exception todo 是否异常
     */
    public static String get(String url) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        ResponseHandler<String> responseHandler = getStringResponseHandler();
        try {
            return httpClient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *     Create a custom response handler
     */
    private static ResponseHandler<String> getStringResponseHandler() {
        return (final HttpResponse response) -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, Consts.UTF_8) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
    }


    /**
     * 获取请求Body
     *
     * @param request ServletRequest
     * @return String
     */
    public static String getBodyString(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}
