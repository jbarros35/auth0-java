package com.auth0.example;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthServletCallback extends AbstractAuthorizationCodeCallbackServlet {

    @Override
    protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
            throws ServletException, IOException {
        resp.addCookie(new Cookie());
        resp.sendRedirect("/hello");
    }

    @Override
    protected void onError(
            HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
            throws ServletException, IOException {
        // handle error
    }

    @Override
    protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
        GenericUrl url = new GenericUrl(req.getRequestURL().toString());
        url.setRawPath("/oauth2callback");
        return url.build();
    }

    @Override
    protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
        return null;
    }

    @Override
    protected AuthorizationCodeFlow initializeFlow() throws IOException {
        return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), new NetHttpTransport(), new JacksonFactory(),
                new GenericUrl("https://pose.auth0.com/oauth/token"),
                new ClientParametersAuthentication("tgqIgqhSmFcWQPNk1S63aFljgbcuF7Qh", "P2CJ7ZQee6SpqEm19WkEHGWqo7jofdPhxfd6Rocp-mzqcciEsxqfJ3naDPaN3mVc"),
                "tgqIgqhSmFcWQPNk1S63aFljgbcuF7Qh", "https://pose.auth0.com/authorize").build();
    }
}
