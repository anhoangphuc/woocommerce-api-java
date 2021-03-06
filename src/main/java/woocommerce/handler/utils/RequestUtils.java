package woocommerce.handler.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestUtils {
    private static final Logger LOG = Logger.getLogger(RequestUtils.class);

    public static String getRequest(String URL, Map<String, String> headers, boolean ignoreSSLError) throws URISyntaxException {
        return getRequest(URL, headers, null, ignoreSSLError);
    }

    public static String getRequest(String URL, Map<String, String> headers) throws URISyntaxException {
        return getRequest(URL, headers, null, false);
    }

    public static String getRequest(String URL, Map<String, String> headers, Map<String, String> params) throws URISyntaxException {
        return getRequest(URL, headers, params, false);
    }

    public static String getRequest(String URL, Map<String, String> headers, Map<String, String> params, boolean ignoreSSLError) throws URISyntaxException {
        String responseData = null;

        if (params != null && params.size() > 0) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            for (String key : params.keySet()) {
                nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
            }
            URL = new URIBuilder(URL).addParameters(nameValuePairs).build().toString();
        }
        HttpGet request = new HttpGet(URL);
        for (String key : headers.keySet()) {
            request.addHeader(key, headers.get(key));
        }
        try (CloseableHttpClient httpClient = getHttpClient(ignoreSSLError);
             CloseableHttpResponse response = httpClient.execute(request)) {
            LOG.debug("Protocol version:" + response.getProtocolVersion());
            LOG.info("Status code: " + response.getStatusLine().getStatusCode());
            LOG.debug("Reason Phrase: " + response.getStatusLine().getReasonPhrase());
            LOG.debug("Status Line: " + response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                responseData = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseData;
    }

    public static String postRequest(String URL, Map<String, String> headers, Map<Object, Object> data, boolean ignoreSSLError) throws UnsupportedEncodingException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(data);
        return postRequest(URL, headers, json, ignoreSSLError);
    }

    public static String postRequest(String URL, Map<String, String> headers, String data, boolean ignoreSSLError) throws UnsupportedEncodingException {
        String responseData = null;
        HttpPost post = new HttpPost(URL);
        for (String key : headers.keySet()) {
            post.addHeader(key, headers.get(key));
        }
        post.setEntity(new StringEntity(data));
        try (CloseableHttpClient httpClient = getHttpClient(ignoreSSLError);
             CloseableHttpResponse response = httpClient.execute(post)) {
            LOG.debug("Protocol version:" + response.getProtocolVersion());
            LOG.info("Status code: " + response.getStatusLine().getStatusCode());
            LOG.debug("Reason Phrase: " + response.getStatusLine().getReasonPhrase());
            LOG.debug("Status Line: " + response.getStatusLine().toString());
            responseData = EntityUtils.toString(response.getEntity());
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
        return responseData;
    }

    public static String deleteRequest(String URL, Map<String, String> headers, boolean ignoreSSLError) {
        String responseData = null;
        HttpDelete delete = new HttpDelete(URL);
        for (String key : headers.keySet()) {
            delete.addHeader(key, headers.get(key));
        }
        try (CloseableHttpClient httpClient = getHttpClient(ignoreSSLError);
             CloseableHttpResponse response = httpClient.execute(delete)) {
            LOG.debug("Protocol version:" + response.getProtocolVersion());
            LOG.info("Status code: " + response.getStatusLine().getStatusCode());
            LOG.debug("Reason Phrase: " + response.getStatusLine().getReasonPhrase());
            LOG.debug("Status Line: " + response.getStatusLine().toString());
            responseData = EntityUtils.toString(response.getEntity());
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
        return responseData;
    }

    public static String putRequest(String URL, Map<String, String> headers, Map<Object, Object> data, boolean ignoreSSLError) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(data);
        return putRequest(URL, headers, json, ignoreSSLError);
    }

    public static String putRequest(String URL, Map<String, String> headers, String data, boolean ignoreSSLError) {
        String responseData = null;
        HttpPut put = new HttpPut(URL);
        for (String key : headers.keySet()) {
            put.addHeader(key, headers.get(key));
        }
        try (CloseableHttpClient httpClient = getHttpClient(ignoreSSLError);
             CloseableHttpResponse response = httpClient.execute(put)) {
            LOG.debug("Protocol version:" + response.getProtocolVersion());
            LOG.info("Status code: " + response.getStatusLine().getStatusCode());
            LOG.debug("Reason Phrase: " + response.getStatusLine().getReasonPhrase());
            LOG.debug("Status Line: " + response.getStatusLine().toString());
            responseData = EntityUtils.toString(response.getEntity());
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
        return responseData;
    }


    static CloseableHttpClient getHttpClient(boolean ignoreSSLError) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return ignoreSSLError ? HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build() : HttpClients.createDefault();
    }
}