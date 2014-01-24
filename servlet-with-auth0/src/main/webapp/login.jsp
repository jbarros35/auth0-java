<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>Login</title>
    <script src="https://d19p4zemcycm7a.cloudfront.net/w2/auth0-widget-2.4.0.min.js"></script>
  </head>
  <body>
    <script type="text/javascript">

      var widget = new Auth0Widget({
        domain:         '<%= application.getInitParameter("auth0.domain") %>',
        clientID:       '<%= application.getInitParameter("auth0.client_id") %>',
        callbackURL:    '<%= application.getInitParameter("auth0.redirect_uri") %>'
      });

    </script>
    <% if ( request.getParameter("error") != null ) { %>
        <span style="color: red;"><c:out value="${param.error}" /> </span>
    <% } %>
    <button onclick="widget.signin()">Login</button>
  </body>
</html>