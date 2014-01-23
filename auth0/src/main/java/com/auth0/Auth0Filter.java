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

    @Override
    public void doFilter(ServletRequest req, ServletResponse response,
                         FilterChain next) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        HttpSession session = ((HttpServletRequest) req).getSession();
        String idToken = (String) session.getAttribute("idToken");
        Object accessToken = session.getAttribute("accessToken");

        // Reject if not accessToken or idToken are found
        if (accessToken == null || idToken == null) {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.reset();
            resp.getWriter().println("Error: You must be logged in to access this page");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        next.doFilter(new Auth0RequestWrapper(idToken, request), response);
    }

    @Override
    public void destroy() {
    }


}
