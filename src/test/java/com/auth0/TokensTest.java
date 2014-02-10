package com.auth0;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokensTest {
    @Test
    public void tokensBasicCase() throws Exception {
        String foo = "foo", bar = "bar";

        Tokens tokens = new Tokens(foo, bar);

        assertEquals(foo, tokens.getIdToken());
        assertEquals(bar, tokens.getAccessToken());
    }

    @Test
    public void tokensOnNullMustNotFail() throws Exception {
        Tokens tokens = new Tokens(null, null);

        assertEquals(null, tokens.getIdToken());
        assertEquals(null, tokens.getAccessToken());
    }
}
