/**
 * 
 */
package com.hi.easydq.proxy.modules.prepostprocessing;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.AuthenticationHeaderMissingException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.AuthenticationProviderConnectionException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.BadRequestException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.NotAuthorizedException;
import com.hi.easydq.proxy.users.AuthenticationProvider;
import com.hi.easydq.proxy.users.Customer;
import com.hi.easydq.proxy.util.AuthenticationUtils;

/**
 * The processor responsible for authenticating the customers.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class AuthenticationPreProcessor implements Processor {

	private static final Logger logger = LoggerFactory
			.getLogger(AuthenticationPreProcessor.class);

	/**
	 * Reference to the authentication provider responsible for performing
	 * chosen authentication method.
	 */
	private final AuthenticationProvider authenticationProvider;

	public AuthenticationPreProcessor(
			AuthenticationProvider authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}

	/**
	 * Reads the request header and extracts authentication header. Decodes the
	 * header and verify the credentials according to specified authentication
	 * provider.
	 */
	@Override
	public HttpObject process(HttpObject msg, Session session)
			throws AuthenticationHeaderMissingException, BadRequestException,
			AuthenticationProviderConnectionException, NotAuthorizedException {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			String authenticationHeader = httpRequest.headers().get(
					HttpHeaders.Names.AUTHORIZATION);
			if (authenticationHeader != null) {
				Customer customer = AuthenticationUtils.decodeAuthenticationHeader(authenticationHeader);
				session.setCustomer(customer);

				try {
					boolean isAuthenticated = authenticationProvider
							.authenticate(customer.getUsername(),
									customer.getPassword());
					if (isAuthenticated) {
						logger.info("User name: " + customer.getUsername()
								+ " authenticated successfully.");
						// Remove the header after authorizing to secure the
						// credentials.
						httpRequest.headers().remove(
								HttpHeaders.Names.AUTHORIZATION);
						return (HttpRequest) httpRequest;
					} else {
						throw new NotAuthorizedException();
					}
				} catch (Exception e) {
					if (e instanceof NotAuthorizedException)
						throw (NotAuthorizedException) e;
					else
						throw new AuthenticationProviderConnectionException(e);
				}
			} else {
				logger.error("Authentication headers not found in the request. Rejecting the request.");
				throw new AuthenticationHeaderMissingException();
			}
		} else {
			throw new BadRequestException();
		}
	}

	/**
	 * Closes internal resources: authentication provider.
	 */
	@Override
	public void close() {
		authenticationProvider.close();
	}

}
