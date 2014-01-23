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

### Maven Dependency

Let's start by adding the Auth0-servlet artifact to the project `pom.xml` file.

> Note: if you are not using Maven you can download the auth0-servlet.jar from the downloads section.

### Authentication

#### Filtering Requests

First, let's start configuring the `web.xml` found in your Web Application. We will start by adding Auth0 configuration parameters:

```xml
<web-app>
    ...
    <!-- Auth0 Configuration -->
    <context-param>
        <param-name>auth0.client_id</param-name>           <param-value>YOUR_CLIENT_ID</param-value>
    </context-param>

    <context-param>
        <param-name>auth0.client_secret</param-name>       <param-value>YOUR_CLIENT_SECRET</param-value>
    </context-param>

    <context-param>
        <param-name>auth0.redirect_uri</param-name>        <param-value>YOUR_REDIRECT_URI</param-value>
    </context-param>

    <context-param>
        <param-name>auth0.domain</param-name>              <param-value>your-domain.auth0.com</param-value>
    </context-param>

    <context-param>
        <!-- This will be the URL where the user is redirected to after logging successfully. -->
        <!-- Make sure to handle that path with a Servlet in the servlet-mapping section -->
        <param-name>auth0.redirect_after</param-name>      <param-value>/hello</param-value>
    </context-param>
</web-app>
```

Then, we are going to add the Auth0Filter which will allow or deny access to certain parts of the application. In this case, restricting access to all paths that match the following regular expression: `/hello/*`.

```xml
<web-app>
    ...
   <!-- Filters -->
    <filter>
        <filter-name>AuthFilter</filter-name>      <filter-class>com.auth0.Auth0Filter</filter-class>
    </filter>
    ...
    <!-- Filter Mapping -->
    <filter-mapping>
        <filter-name>AuthFilter</filter-name>      <url-pattern>/hello/*</url-pattern>
    </filter-mapping>

</web-app>
```

The default behavior will be as follows:
* Send a HTTP 403 Forbidden Status code if the user is not logged in. This can be changed to redirect the user to a given URL (for instance a JSP that contains an Auth0 Widget).
* Prepare a [Principal](http://docs.oracle.com/javase/6/docs/api/java/security/Principal.html) instance that will contain your Auth0 user data. You can access that later from your `Servlet` class by calling the `getUserPrincipal` method.

#### Callback URL

In order to communicate with Auth0 we will need to expose a callback URI where the user will be redirected:

```xml
<web-app>
    ...
    <!-- Servlets -->
    <servlet>
        <servlet-name>RedirectCallback</servlet-name>      <servlet-class>com.auth0.Auth0ServletCallback</servlet-class>
    </servlet>
    ...
    <!-- Servlet Mappings -->
    <servlet-mapping>
        <servlet-name>RedirectCallback</servlet-name>      <url-pattern>/oauth2callback/*</url-pattern>
    </servlet-mapping>

</web-app>
```

In the `Auth0ServletCallback` the data to popuplate principal will be persisted in session. As we will see later this can be customized.

As we have configured previously, the user will be redirected to `/hello`. User-provided `HelloServlet`, which overrides `doGet` method, will be handling that case:

```xml
<web-app>
    ...
    <!-- Servlets -->
    ...
    <servlet>
        <servlet-name>Hello</servlet-name>       <servlet-class>com.auth0.example.HelloServlet</servlet-class>
    </servlet>

    <!-- Servlet Mappings -->
    ...
    <servlet-mapping>
        <servlet-name>Hello</servlet-name>      <url-pattern>/hello/*</url-pattern>
    </servlet-mapping>
</web-app>
```

#### Login page

Last but not least, one more Servlet is needed to handle the login page. We are going to use the `Auth0LoginServlet` class which contains a simple Auth0 Widget.

```xml
    <!-- Servlets -->
    <servlet>
        <servlet-name>Login</servlet-name>      <servlet-class>com.auth0.Auth0LoginServlet</servlet-class>
    </servlet>

    <!-- Servlet Mappings -->
    <servlet-mapping>
        <servlet-name>Login</servlet-name>      <url-pattern>/login</url-pattern>
    </servlet-mapping>
```

Point your browser to `/login` and you will be seeing that login page.

### Extensibility points

On the first part, we explained how to get running up and fast with Auth0 in your app. But, probably, you needed some degree of customization over any of the involved parts. We will see how to customize it to better suit your needs.

In order to handle the callback call from Auth0, you will need to have a Servlet that handles the request.

#### Auth0 Filter

`Auth0 Filter` can be subclassed and the following `protected` methods meant to be overriden:
```java
protected void onSuccess(ServletRequest req, ServletResponse resp, FilterChain next, Tokens tokens) throws IOException, ServletException {

}```


o we override the `doGet` method of the servlet to handle the Auth0 incoming request:

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

A possible implementation of `parseTokensAndSaveToSession` using [Jackson](http://jackson.codehaus.org/), a JSON parsing library, will look like this:

```js
private void parseTokensAndSaveToSession(String body, HttpSession session) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> jsonAsMap = mapper.readValue(body, Map.class);
    String accessToken = jsonAsMap.get("access_token");
    String idToken = jsonAsMap.get("id_token");

    session.setAttribute("accessToken", accessToken);
    session.setAttribute("idToken", idToken);
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
