package com.auth0;

import static java.util.Collections.emptyEnumeration;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.junit.Test;

public class Auth0ServletCallbackTest {

    @Test(expected = IllegalArgumentException.class)
    public void failOnMissingInitParameters() throws ServletException {
        final Auth0ServletCallback cb = new Auth0ServletCallback();
        cb.init(mock(ServletConfig.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failOnMissingServletContextParameters() throws ServletException {
        final Auth0ServletCallback cb = new Auth0ServletCallback();
        final ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameter(eq("auth0.redirect_on_success"))).thenReturn("");
        when(config.getInitParameter(eq("auth0.redirect_on_error"))).thenReturn("");
        final ServletContext ctx = mock(ServletContext.class);
        when(ctx.getInitParameterNames()).thenReturn(emptyEnumeration());
        when(config.getServletContext()).thenReturn(ctx);
        cb.init(config);
    }
}
