package com.auth0.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AuthServletCallback extends HttpServlet {

    private void parseTokensAndSaveToSession(String body, HttpSession session) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> jsonAsMap = mapper.readValue(body, Map.class);
        String accessToken = jsonAsMap.get("access_token");
        String idToken = jsonAsMap.get("id_token");

        session.setAttribute("accessToken", accessToken);
        session.setAttribute("idToken", idToken);
    }


    private URI getURI(Properties properties) {
        URI https;
        try {
            https = new URI("https", (String) properties.get("auth.domain"), "/oauth/token", "");

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return https;
    }

    private String getAuthorizationCode(HttpServletRequest req) {
        String authorizationCode = null;
        List<NameValuePair> queryString = URLEncodedUtils.parse(req.getQueryString(), Charset.forName("ASCII"));

        for (NameValuePair pair : queryString) {
            if ("code".equals(pair.getName())) {
                authorizationCode = pair.getValue();
            }
        }
        return authorizationCode;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Parse request to fetch authorization code
        String authorizationCode = getAuthorizationCode(req);

        Properties properties = new Properties();
        properties.load(getServletContext().getResourceAsStream("/WEB-INF/auth.properties"));

        URI accessTokenURI = getURI(properties);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(accessTokenURI);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("client_id",      (String) properties.get("auth.client_id")));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri",   (String) properties.get("auth.redirect_uri")));
        nameValuePairs.add(new BasicNameValuePair("client_secret",  (String) properties.get("auth.client_secret")));
        nameValuePairs.add(new BasicNameValuePair("code",           authorizationCode));
        nameValuePairs.add(new BasicNameValuePair("grant_type",     "authorization_code"));
        nameValuePairs.add(new BasicNameValuePair("scope",          "openid"));

        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        CloseableHttpResponse response = httpClient.execute(httpPost);

        String tokensToParse;
        try {
            HttpEntity entity = response.getEntity();
            tokensToParse = EntityUtils.toString(entity);

            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        // Parse and obtain both access token and id token and save them in a persistent session
        parseTokensAndSaveToSession(tokensToParse, req.getSession());

        // Redirect user to home
        resp.sendRedirect("/hello");
    }
}
