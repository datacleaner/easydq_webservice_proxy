package com.hi.easydq.proxy.util;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

import com.hi.easydq.proxy.users.Customer;

/**
 * Utils class for authentication methods.
 * 
 * @author Tomasz Guzialek
 *
 */
public class AuthenticationUtils {

	/**
	 * Extracts the credentials from Base64-encoded form in the request header.
	 * 
	 * @param value Base64-encoded header value.
	 * @return The {@link Customer} object filled with credentials.
	 */
	public static Customer decodeAuthenticationHeader(String value) {
		String pureValue = value.substring("Basic".length()).trim();
		String decodedAuthorizationHeader = new String(
				Base64.decodeBase64(pureValue), Charset.forName("UTF-8"));
		String userName = decodedAuthorizationHeader.substring(0,
				decodedAuthorizationHeader.indexOf(':'));
		String password = decodedAuthorizationHeader
				.substring(decodedAuthorizationHeader.indexOf(':') + 1);
		Customer customer = new Customer(userName, password);
		return customer;
	}
	
}
