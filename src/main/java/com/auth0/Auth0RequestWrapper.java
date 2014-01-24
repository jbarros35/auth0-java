package com.auth0;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

public class Auth0RequestWrapper extends HttpServletRequestWrapper {
    String idToken;
    HttpServletRequest realRequest;

    public Auth0RequestWrapper(Tokens tokens, HttpServletRequest request) {
        super(request);

        this.idToken = tokens.getIdToken();
        this.realRequest = request;
    }

    @Override
    public Principal getUserPrincipal() {
        if (this.idToken == null) {
            return realRequest.getUserPrincipal();
        }

        return new Auth0Principal(this.idToken);
    }

}
