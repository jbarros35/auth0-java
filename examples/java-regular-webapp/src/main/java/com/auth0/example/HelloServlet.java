package com.auth0.example;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.Auth0User;

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
                "  </head>\n" +
                "  <body>\n");
        
        Auth0User user = Auth0User.get(request);

        resp.getWriter().println("<h1>Welcome</h1>");
        resp.getWriter().println("<img src=\"" + user.getPicture() + "\" />");
        resp.getWriter().println("<p>Hello " + request.getUserPrincipal().getName() + "!</p>");
        resp.getWriter().println("<p>Your nickname is: " + user.getNickname() + "</p>");
        resp.getWriter().println("  </body>\n" +
                "</html>");
    }
}
