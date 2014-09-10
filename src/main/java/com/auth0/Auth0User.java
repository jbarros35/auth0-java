package com.auth0;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class Auth0User implements Principal {
	
	private JSONObject json;

	public Auth0User(JSONObject json) {
		this.json = json;
	}
	
	public static Auth0User get(HttpServletRequest req) {
		return (Auth0User) req.getSession().getAttribute("user");
	}
	
	public String getProperty(String prop) {
		return get(prop, String.class);
	}
	
	private <T> T get(String prop, Class<T> clazz) {
		try {
			return (T) json.get(prop);
		} catch(JSONException ex) {
			throw new IllegalStateException("Cannot get property " + prop + " from Auth0user", ex);
		}
	}
	
	public String getName() {
		return getProperty("name");
	}
	
	public String getEmail() {
		return getProperty("email");
	}
	
	public String getUserId() {
		return getProperty("user_id");
	}
	
	public String getNickname() {
		return getProperty("nickname");
	}
	
	public String getPicture() {
		return getProperty("picture");
	}
	
	public JSONArray getIdentities() {
		return get("identities", JSONArray.class);
	}
	
	

}
