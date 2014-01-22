<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>Login</title>
    <script src="https://d19p4zemcycm7a.cloudfront.net/w2/auth0-widget-2.4.0.min.js"></script>
  </head>
  <body>
    <script type="text/javascript">

    <%@page import="java.io.InputStream" %>
    <%@page import="java.util.Properties" %>
    <%
        InputStream stream = application.getResourceAsStream("/WEB-INF/auth.properties");
        Properties props = new Properties();
        props.load(stream);
    %>

      var widget = new Auth0Widget({
        domain:         '<%= props.get("auth.domain") %>',
        clientID:       '<%= props.get("auth.client_id") %>',
        callbackURL:    '<%= props.get("auth.redirect_uri") %>'
      });

    </script>
    <button onclick="widget.signin()">Login</button>
  </body>
</html>