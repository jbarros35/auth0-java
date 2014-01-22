package com.auth0.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class AuthRequestWrapper extends HttpServletRequestWrapper {
    Map<String, String> user;
    HttpServletRequest realRequest;

    public AuthRequestWrapper(String idToken, HttpServletRequest request) throws IOException {
        super(request);

        String[] split = idToken.split("\\.");

        if (split.length != 3) {
            throw new RuntimeException("split should have three parts");
        }

        String userInfoJson = new String(Base64.decodeBase64(split[1]));
        ObjectMapper mapper = new ObjectMapper();
        this.user = mapper.readValue(userInfoJson, Map.class);
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
