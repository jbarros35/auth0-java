package com.auth0;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

public class Auth0Principal implements Principal {

    private final Map<String, String> user;

    public Auth0Principal(String idToken) {
        if (idToken == null) {
            throw new RuntimeException("idToken cannot be null");
        }

        String[] split = idToken.split("\\.");

        if (split.length != 3) {
            throw new RuntimeException("Split should have three parts");
        }

        String userInfoJson = new String(Base64.decodeBase64(split[1]));
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.user = mapper.readValue(userInfoJson, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing Id Token JSON");
        }
    }

    public Map<String, String> getUser() {
        return Collections.unmodifiableMap(user);
    }

    @Override
    public String getName() {
        return user.get("name");
    }

    public String getMail() {
        return user.get("email");
    }

    @Override
    public String toString() {
        return user.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Auth0Principal that = (Auth0Principal) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }
}
