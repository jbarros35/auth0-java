package com.auth0;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class Auth0Filter implements Filter {

    private String onFailRedirectTo;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        onFailRedirectTo = filterConfig.getInitParameter("auth0.redirect_on_authentication_error");

        if (onFailRedirectTo == null) {
            throw new IllegalArgumentException("auth0.redirect_on_authentication_error parameter of " + this.getClass().getName() + " cannot be null");
        }
    }

    protected Tokens loadTokens(ServletRequest req, ServletResponse resp) {
        HttpSession session = ((HttpServletRequest) req).getSession();
        return new Tokens((String) session.getAttribute("idToken"),
                (String) session.getAttribute("accessToken"));
    }

    protected void onSuccess(ServletRequest req, ServletResponse resp, FilterChain next, Tokens tokens) throws IOException, ServletException {
        Auth0RequestWrapper auth0RequestWrapper = new Auth0RequestWrapper(tokens, (HttpServletRequest) req);
        next.doFilter(auth0RequestWrapper, resp);
    }

    protected void onReject(ServletRequest req, ServletResponse response, FilterChain next) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.sendRedirect(onFailRedirectTo);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain next) throws IOException, ServletException {

        Tokens tokens = loadTokens(req, resp);

        // Reject if not accessToken or idToken are found
        if (tokens.getAccessToken() == null || tokens.getIdToken() == null) {
            onReject(req, resp, next);
            return;
        }

        onSuccess(req, resp, next, tokens);
    }

    @Override
    public void destroy() {
    }
}
