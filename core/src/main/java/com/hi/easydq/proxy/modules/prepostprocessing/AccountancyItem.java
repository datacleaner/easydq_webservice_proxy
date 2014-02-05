package com.hi.easydq.proxy.modules.prepostprocessing;

import java.util.Date;

import com.hi.easydq.proxy.users.Customer;

/**
 * A class used as an entity for storing information about a single successful
 * request handled for billing purposes.
 * 
 * @author Tomasz Guzialek
 * 
 */
/**
 * @author Tomasz Guzialek
 * 
 */
public class AccountancyItem {

	/**
	 * The customer using the service.
	 */
	private final Customer customer;

	/**
	 * The name of the web service.
	 */
	private String serviceName;

	/**
	 * The time stamp of the request.
	 */
	private Date timeStamp;

	public AccountancyItem(Customer customer, String serviceName) {
		this.customer = customer;
		this.serviceName = serviceName;
		this.timeStamp = new Date();
	}

	public AccountancyItem(Customer customer, String serviceName, Date timeStamp) {
		this.customer = customer;
		this.serviceName = serviceName;
		this.timeStamp = timeStamp;
	}

	public Customer getCustomer() {
		return customer;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Prints the accountancy item internal information (customer, service name
	 * and the time stamp) in a human-readable form.
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(super.toString());
		stringBuilder.append(" = { customer: ");
		stringBuilder.append(this.customer.getUsername());
		stringBuilder.append(", serviceName: ");
		stringBuilder.append(serviceName);
		stringBuilder.append(", timeStamp: ");
		stringBuilder.append(timeStamp);
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

}
