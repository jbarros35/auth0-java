package com.auth0.example;

import com.auth0.Auth0Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
                "    <title>Login</title>\n" +
                "    <script src=\"https://cdn.auth0.com/w2/auth0-widget-2.4.0.min.js\"></script>\n" +
                "  </head>\n" +
                "  <body>\n");

        resp.getWriter().println("<h1>Welcome</h1>");
        resp.getWriter().println("<p>Hello " + request.getUserPrincipal().getName() + "!</p>");
        resp.getWriter().println("<p>Your email is: " + ((Auth0Principal) request.getUserPrincipal()).getMail() + "</p>");
        resp.getWriter().println("  </body>\n" +
                "</html>");
    }
}
