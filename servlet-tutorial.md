## Servlet-based Application Tutorial

This guide will walk you through adding authentication to an existing Java Web Application that is based on [Java Servlet Technology](http://www.oracle.com/technetwork/java/index-jsp-135475.html). 

In case you are starting from scratch, you can create a Java Web Application by using the `maven-archetype-webapp`

```
mvn archetype:generate -DgroupId=com.acme \
                       -DartifactId=my-webapp \
                       -Dversion=1.0-SNAPSHOT \
                       -DarchetypeArtifactId=maven-archetype-webapp \
                       -DinteractiveMode=false

```

### Authentication

First, let's create a [Filter](http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html) that will work as an interceptor or middleware. There, we will handle the incoming request before passing it to the servlets you want to restrict access.

Let's name it `AuthFilter` as it will handle **Authentication** aspects. Its doFilter method will look like this:

```java
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

    next.doFilter(new AuthRequestWrapper(idToken, request), response);
}

```

When a request enters the filter, the following will happen:

* Send a HTTP 403 Forbidden Status code if the user is not logged in. This can be changed to redirect the user to a given URL (for instance a JSP that contains an Auth0 Widget).
* Prepare a [Principal](http://docs.oracle.com/javase/6/docs/api/java/security/Principal.html) instance that will contain your Auth0 user data. You can access that later from your Servlet class by calling the `getUserPrincipal` method. That class can be extended to include other Auth0 data such as email and given name.

### Callback URL
In order to handle the callback call from Auth0, you will need to have a Servlet that handles the request. So we override the `doGet` method of the servlet to handle the Auth0 incoming request:

```java
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // Parse request to fetch authorization code
    String authorizationCode = getAuthorizationCode(req);

    CloseableHttpClient httpClient = HttpClients.createDefault();

    HttpPost httpPost = new HttpPost("https://your-app.auth0.com/oauth/token");
    
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("client_id",      "YOUR_CLIENT_ID"));
    nameValuePairs.add(new BasicNameValuePair("redirect_uri",   "http://your/redirect/uri"));
    nameValuePairs.add(new BasicNameValuePair("client_secret", "YOUR_CLIENT_SECRET"));
    nameValuePairs.add(new BasicNameValuePair("code",           "YOUR_AUTHORIZATION_CODE"));
    nameValuePairs.add(new BasicNameValuePair("grant_type",     "authorization_code"));
    nameValuePairs.add(new BasicNameValuePair("scope",          "openid"));
    
    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    CloseableHttpResponse response = httpClient.execute(httpPost);

    String tokensToParse;
    try {
        HttpEntity entity = response.getEntity();
        tokensToParse = EntityUtils.toString(entity);

        EntityUtils.consume(entity);
    } finally {
        response.close();
    }

// Parse and obtain both access token and id token and save them in a persistent session
    parseTokensAndSaveToSession(tokensToParse, req.getSession());
    
    // Redirect user to home
    resp.sendRedirect("/hello");
}

```


### Login Page


If your Application has an HTML frontend you will need to add the [Auth0 Widget](https://docs.auth0.com/login-widget2) to your login jsp.

```jsp
<!DOCTYPE html>
<html>
  <head>
    <title>Login</title>
    <script src="https://d19p4zemcycm7a.cloudfront.net/w2/auth0-widget-2.4.0.min.js"></script>
  </head>
  <body>
    <script type="text/javascript">

      var widget = new Auth0Widget({
        domain:         'your-app.auth0.com',
        clientID:       'YOUR_CLIENT_ID',
        callbackURL:    'http://your/callback/'
      });

    </script>
    <button onclick="widget.signin()">Login</button>
  </body>
</html>
```
