package com.auth0.example;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class AuthServlet extends AbstractAuthorizationCodeServlet {
    @Override
    protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
        return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), new NetHttpTransport(), new JacksonFactory(),
                new GenericUrl("https://pose.auth0.com/oauth/token"),
                new ClientParametersAuthentication("tgqIgqhSmFcWQPNk1S63aFljgbcuF7Qh", "P2CJ7ZQee6SpqEm19WkEHGWqo7jofdPhxfd6Rocp-mzqcciEsxqfJ3naDPaN3mVc"),
                "tgqIgqhSmFcWQPNk1S63aFljgbcuF7Qh", "https://pose.auth0.com/authorize").build();
    }

    @Override
    protected String getRedirectUri(HttpServletRequest httpServletRequest) throws ServletException, IOException {
        GenericUrl url = new GenericUrl(httpServletRequest.getRequestURL().toString());
        url.setRawPath("/oauth2callback");
        return url.build();
    }

    @Override
    protected String getUserId(HttpServletRequest httpServletRequest) throws ServletException, IOException {
        return null;
    }
}
