package com.auth0;

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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

public class Auth0ServletCallback extends HttpServlet {

    private Properties properties = new Properties();

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
            https = new URI("https", (String) properties.get("auth0.domain"), "/oauth/token", "");

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

    /**
     * Fetch properties to be used. User is encourage to override this method.
     *
     * Auth0 uses the ServletContext parameters:
     *
     * <dl>
     *     <dt>auth0.client_id</dd><dd>Application client id</dd>
     *     <dt>auth0.client_secret</dt><dd>Application client secret</dd>
     *     <dt>auth0.redirect_uri</dt><dd>OAuth2 URI to redirect the user with Authorization Code.
     *     In most cases, this will be the route to com.auth0.Auth0ServletCallback servlet (or your subclass of it).
     *     For instance, if your servlet will be routed to /callback and you will be listening on
     *     localhost port 8080 it should be http://localhost:8080/callback</dd>
     *     <dt>auth0.domain</dt><dd>Auth0 domain</dd>
     *     <dt>auth0.redirect_after</dt><dd>Where to send the user after successful login.</dd>
     * </dl>
     */
    @Override
    public void init() throws ServletException {
        Map<String, Boolean> requiredParametersChecklist = new HashMap<String, Boolean>();
        StringBuilder missingParameters = new StringBuilder();
        String [] requiredParametersName = {
                "auth0.client_id", "auth0.client_secret", "auth0.redirect_uri",
                "auth0.domain", "auth0.redirect_after"};


        for (String requiredParameterName : requiredParametersName) {
            requiredParametersChecklist.put(requiredParameterName, false);
        }

        Enumeration initParameterNames = getServletContext().getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String key = (String) initParameterNames.nextElement();
            String value = getServletContext().getInitParameter(key);

            properties.put(key, value);

            requiredParametersChecklist.put(key, true);
        }

        for (Map.Entry<String, Boolean> entry : requiredParametersChecklist.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();
            if (!value) {
                if (missingParameters.length() == 0) {
                    missingParameters.append("Error: The following required ServletContext parameters where not found: ");
                }
                missingParameters.append(key);
                missingParameters.append(", ");
            }
        }


        if (missingParameters.length() > 0) {
            // Removing last ", "
            missingParameters.setLength(missingParameters.length() - 2);

            missingParameters.append(". They should be present in web.xml in a <context-param> tag.");
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Parse request to fetch authorization code
        String authorizationCode = getAuthorizationCode(req);

        URI accessTokenURI = getURI(properties);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(accessTokenURI);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("client_id",      (String) properties.get("auth0.client_id")));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri",   (String) properties.get("auth0.redirect_uri")));
        nameValuePairs.add(new BasicNameValuePair("client_secret",  (String) properties.get("auth0.client_secret")));
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
        resp.sendRedirect((String) properties.get("auth0.redirect_after"));
    }
}
