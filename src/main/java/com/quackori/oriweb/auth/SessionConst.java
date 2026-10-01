package com.quackori.oriweb.auth;

public final class SessionConst {

	/** Session attribute key for the logged-in user's ID */
	public static final String LOGIN_USER_ID = "LOGIN_USER_ID";

	/** Session attribute key for GitHub account info waiting for signup confirmation */
	public static final String OAUTH_PENDING_SIGNUP = "OAUTH_PENDING_SIGNUP";

	private SessionConst() {
	}

}
