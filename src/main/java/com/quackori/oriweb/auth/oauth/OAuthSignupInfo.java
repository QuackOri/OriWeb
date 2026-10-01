package com.quackori.oriweb.auth.oauth;

/**
 * Shown on the signup confirmation page.
 *
 * @param provider provider name (e.g. github)
 * @param login    login name from the provider
 * @param username username that will be created
 */
public record OAuthSignupInfo(String provider, String login, String username) {
}
