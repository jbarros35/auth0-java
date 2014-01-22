# Auth0 and Java

These Java examples will help you implement server-side (API) authentication:

 * [Servlet example](https://github.com/auth0/auth0-java/tree/master/servlet-with-auth0)


You can use it together with [Auth0](https://www.auth0.com) to add username/password authentication, support for enterprise identity like Active Directory or SAML and also for social identities like Google, Facebook or Salesforce among others to your web, API and mobile native apps. 


[Auth0](https://www.auth0.com) is a cloud service that provides a turn-key solution for authentication, authorization and Single Sign On.


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

---

## What is Auth0?

Auth0 helps you to:

* Add authentication with [multiple authentication sources](https://docs.auth0.com/identityproviders), either social like **Google, Facebook, Microsoft Account, LinkedIn, GitHub, Twitter, Box, Salesforce, amont others**, or enterprise identity systems like **Windows Azure AD, Google Apps, Active Directory, ADFS or any SAML Identity Provider**. 
* Add authentication through more traditional **[username/password databases](https://docs.auth0.com/mysql-connection-tutorial)**.
* Add support for **[linking different user accounts](https://docs.auth0.com/link-accounts)** with the same user.
* Support for generating signed [Json Web Tokens](https://docs.auth0.com/jwt) to call your APIs and **flow the user identity** securely.
* Analytics of how, when and where users are logging in.
* Pull data from other sources and add it to the user profile, through [JavaScript rules](https://docs.auth0.com/rules).

## Create a free account in Auth0

1. Go to [Auth0](http://developers.auth0.com) and click Sign Up.
2. Use Google, GitHub or Microsoft Account to login.

## License

The MIT License (MIT)

Copyright (c) 2013 AUTH10 LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

