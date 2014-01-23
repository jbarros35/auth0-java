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

First, let's start configuring the `web.xml` found in your Web Application. We will start by adding Auth0 configuration parameters:

```xml
<web-app
        xmlns="http://java.sun.com/xml/ns/javaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
        version="2.5">
        
    ...
    
    <!-- Auth0 Configuration -->
    <context-param>
        <param-name>auth0.client_id</param-name>
        <param-value>YOUR_CLIENT_ID</param-value>
    </context-param>

    <context-param>
        <param-name>auth0.client_secret</param-name>
        <param-value>YOUR_CLIENT_SECRET</param-value>
    </context-param>

    <context-param>
        <param-name>auth0.redirect_uri</param-name>
        <param-value>YOUR_REDIRECT_URI</param-value>
    </context-param>

    <context-param>
        <param-name>auth0.domain</param-name>
        <param-value>your-domain.auth0.com</param-value>
    </context-param>

    <context-param>
        <param-name>auth0.redirect_after</param-name>
        <param-value>/path/in/your/app</param-value>
    </context-param>
</web-app>
```

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
