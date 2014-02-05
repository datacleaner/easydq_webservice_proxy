package com.hi.easydq.proxy.users;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.NotAuthorizedException;

/**
 * The authentication provider connecting to an LDAP server.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class LdapAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(LdapAuthenticationProvider.class);

	/**
	 * Object representing connection to LDAP server
	 */
	private final LdapConnection connection;

	/**
	 * Google Guava Cache implementation.
	 */
	private LoadingCache<String, String> cache;

	/**
	 * The username used for connecting to the LDAP server.
	 */
	private String userName;

	/**
	 * The password used for connecting to the LDAP server.
	 */
	private String password;

	/**
	 * String representing an escaped forward slash sign '\'
	 */
	private final static String SLASH_SIGN_CONVERTED = "\\5c";

	/**
	 * String representing an escaped * sign '*'
	 */
	private final static String STAR_SIGN_CONVERTED = "\\2a";

	/**
	 * String representing an escaped opening bracket sign '('
	 */
	private final static String OPENING_BRACKET_SIGN_CONVERTED = "\\28";

	/**
	 * String representing an escaped closing bracket sign ')'
	 */
	private final static String CLOSING_BRACKET_SIGN_CONVERTED = "\\29";

	public LdapAuthenticationProvider(String host, int port, String userName,
			String password) {
		this.userName = userName;
		this.password = password;

		this.connection = new LdapNetworkConnection(host, port);
	}

	public LdapAuthenticationProvider(LdapConnection connection,
			String userName, String password) {
		this.userName = userName;
		this.password = password;

		this.connection = connection;
		connection.setTimeOut(30000); // Default value in the Apache LDAP API
	}

	/**
	 * Connects to the LDAP server (bind method).
	 */
	@Override
	public void connect() throws LdapException {
		logger.info("Connecting to LDAP server...");
		connection.bind(userName, password);
		logger.info("Connected to LDAP server.");
	}

	/**
	 * Firstly checks if the username is not in the cache. If not, executes a
	 * query to the LDAP server. If authorized, stores the credentials in the
	 * cache.
	 */
	@Override
	public boolean authenticate(String userName, String password) {
		if (cache == null) {
			final String passwordFinal = password;

			this.cache = CacheBuilder.newBuilder()
					.expireAfterAccess(10, TimeUnit.SECONDS)
					.build(new CacheLoader<String, String>() {

						@Override
						public String load(String username) throws Exception {
							boolean authenticated = authenticateInLdap(
									username, passwordFinal);
							if (authenticated) {
								return passwordFinal;
							} else {
								throw new NotAuthorizedException();
							}
						}
					});
		}
		String passwordExpected;
		try {
			passwordExpected = cache.get(userName);
			return passwordExpected.equals(password);
		} catch (ExecutionException e) {
			return false;
		}
	}

	/**
	 * Performs the lookup in the LDAP server.
	 * 
	 * @param userName The username being checked.
	 * @param password The password being checked.
	 * @return The authentication result.
	 * @throws LdapException
	 */
	private boolean authenticateInLdap(String userName, String password)
			throws LdapException {
		// connect(); // Not used anymore as connection is established while
		// initializing the proxy.
		EntryCursor cursor;
		try {
			cursor = connection.search("ou=users,ou=system", "(uid="
					+ escapeUserName(userName) + ")", SearchScope.SUBTREE);
			while ((cursor != null) && (cursor.next())) {
				Entry entry = cursor.get();

				String uid = entry.get("uid").getString();
				String userpassword = entry.get("userpassword").getString();

				if ((uid.equals(userName)) && (userpassword.equals(password))) {
					cache.put(userName, password);
					cursor.close();
					// close();
					return true;
				}
			}
		} catch (CursorException e) {
			logger.error("Cursor exception while querying LDAP server for user name: "
					+ userName);
			close();
		} catch (LdapException e) {
			logger.error("LdapException while querying LDAP server for user name: "
					+ userName);
			close();
		}
		// close();
		return false;
	}

	/**
	 * The method to escape user name input in order to prevent LDAP injection.
	 * Slash sign, start sign, left and right bracket signs are converted.
	 * 
	 * @param userName
	 *            User input to escape
	 * @return Escaped string
	 */
	private String escapeUserName(String userName) {
		StringBuffer escapedUserName = new StringBuffer();
		for (int i = 0; i < userName.length();) {
			switch (userName.charAt(i)) {
			case '\\':
				escapedUserName.append(SLASH_SIGN_CONVERTED);
				i++;
				break;

			case '*':
				escapedUserName.append(STAR_SIGN_CONVERTED);
				i++;
				break;

			case '(':
				escapedUserName.append(OPENING_BRACKET_SIGN_CONVERTED);
				i++;
				break;

			case ')':
				escapedUserName.append(CLOSING_BRACKET_SIGN_CONVERTED);
				i++;
				break;

			default:
				escapedUserName.append(userName.charAt(i));
				i++;

			}
		}

		return escapedUserName.toString();
	}

	/**
	 * Unbinds and closes the connection the LDAP server.
	 */
	@Override
	public void close() {
		try {
			logger.info("Closing LDAP connection...");
			connection.unBind();
			connection.close();
			logger.info("LDAP connection closed.");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
