package com.quackori.oriweb.auth.oauth;

import java.io.Serializable;

/**
 * GitHub account info kept in the server-side session between GitHub authentication
 * and the user confirming the signup. Never sent to or accepted from the client.
 *
 * @param provider   provider name (e.g. github)
 * @param providerId unique user ID from the provider (GitHub user number)
 * @param login      login name from the provider (GitHub login)
 */
public record PendingOAuthSignup(String provider, String providerId, String login) implements Serializable {
}
