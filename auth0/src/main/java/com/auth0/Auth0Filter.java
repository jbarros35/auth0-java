package com.auth0;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class Auth0Filter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    protected Tokens loadTokens(ServletRequest req, ServletResponse resp) {
        HttpSession session = ((HttpServletRequest) req).getSession();
        return new Tokens((String) session.getAttribute("idToken"),
                (String) session.getAttribute("accessToken"));
    }

    private void onSuccess(ServletRequest req, ServletResponse resp, FilterChain next, Tokens tokens) throws IOException, ServletException {
        Auth0RequestWrapper auth0RequestWrapper = new Auth0RequestWrapper(tokens, (HttpServletRequest) req);
        next.doFilter(auth0RequestWrapper, resp);
    }

    protected void onReject(ServletRequest req, ServletResponse response, FilterChain next) throws IOException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.reset();
        resp.getWriter().println("Error: You must be logged in to access this page");
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
