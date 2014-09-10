package com.auth0;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;

public class RequestNonceStorageTest {

    @Test
    public void setAndGetState() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("state")).thenReturn("somestate");

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getSession(anyBoolean())).thenReturn(session);

        RequestNonceStorage storage = new RequestNonceStorage(httpServletRequest);
        storage.setState("somestate");

        verify(session).setAttribute("state", "somestate");

        assertEquals("somestate", storage.getState());
    }
}