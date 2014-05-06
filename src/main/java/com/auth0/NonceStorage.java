package com.auth0;

public interface NonceStorage {
    String getState();

    void setState(String nonce);
}
