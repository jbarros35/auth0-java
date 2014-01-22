package com.auth0.example;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class AuthRequestWrapper extends HttpServletRequestWrapper {
    Map<String, String> user;
    HttpServletRequest realRequest;

    public AuthRequestWrapper(Map<String, String> user, HttpServletRequest request) {
        super(request);
        this.user = user;
        this.realRequest = request;
    }

    @Override
    public Principal getUserPrincipal() {
        if (this.user == null) {
            return realRequest.getUserPrincipal();
        }

        return new Principal() {

            @Override
            public String getName() {
                return user.get("name");
            }
        };
    }
}
