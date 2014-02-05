package com.hi.easydq.proxy.users;

import java.io.Closeable;

/**
 * An interface representing a connection to external resource for authentication purposes.
 * 
 * @author Tomasz Guzialek
 *
 */
public interface AuthenticationProvider extends Closeable {

	/**
	 * The start-up method. Connects to the resource.
	 * 
	 * @throws Exception
	 */
	public void connect() throws Exception;

	/**
	 * Verifies if specified credentials are valid.
	 * 
	 * @param userName The username.
	 * @param password The password associated with the username.
	 * @return True if valid, false if invalid.
	 * @throws Exception
	 */
	public boolean authenticate(String userName, String password) throws Exception;

	/**
	 * The method for closing internal resources before destructing the object.
	 */
	@Override
	public void close();
}
