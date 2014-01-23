package com.auth0;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

public class Auth0Principal implements Principal {

    private final Map<String, String> user;

    public Auth0Principal(Map<String, String> user) {
        this.user = user;
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
}
