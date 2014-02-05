package com.hi.easydq.proxy.users;

/**
 * The class representing a customer - user of the web services.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class Customer {

	/**
	 * Human-friendly user name. 
	 */
	private final String userName;
	
	/**
	 * User password.
	 */
	private final String password;

	public Customer(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getUsername() {
		return userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	/**
	 * Compares the username fields of the objects to determine equality.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Customer) {
			Customer customer = (Customer) obj; 
			return this.userName.equals(customer.userName);
		} else {
			return false;
		}
	}
	
	/**
	 * Does NOT override the superimplementation.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
