package com.auth0;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Auth0LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String domain       = StringEscapeUtils.escapeEcmaScript(getServletContext().getInitParameter("auth0.domain"));
        String clientId     = StringEscapeUtils.escapeEcmaScript(getServletContext().getInitParameter("auth0.client_id"));
        String redirectURI  = StringEscapeUtils.escapeEcmaScript(getServletContext().getInitParameter("auth0.redirect_uri"));

        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
                "    <title>Login</title>\n" +
                "    <script src=\"https://d19p4zemcycm7a.cloudfront.net/w2/auth0-widget-2.4.0.min.js\"></script>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <script type=\"text/javascript\">\n" +
                "\n" +
                "      var widget = new Auth0Widget({\n" +
                "        domain:         '" +   domain      + "',\n" +
                "        clientID:       '" +   clientId    + "',\n" +
                "        callbackURL:    '" +   redirectURI + "'\n" +
                "      });\n" +
                "\n" +
                "    </script>\n" +
                "    <button onclick=\"widget.signin()\">Login</button>\n" +
                "  </body>\n" +
                "</html>");
    }
}
