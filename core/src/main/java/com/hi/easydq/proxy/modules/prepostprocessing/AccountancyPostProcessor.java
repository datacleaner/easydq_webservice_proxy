package com.hi.easydq.proxy.modules.prepostprocessing;

import io.netty.handler.codec.http.HttpObject;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.users.Customer;

/**
 * The postprocessor responsible for accounting the customers' usage.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class AccountancyPostProcessor implements Processor {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(AccountancyPostProcessor.class);

	/**
	 * The reference to the {@link Accountancy} information storage.
	 */
	private final Accountancy accountancy;

	public AccountancyPostProcessor(Accountancy accountancy) {
		this.accountancy = accountancy;
	}

	/**
	 * Adds a new item to accountancy.
	 */
	@Override
	public HttpObject process(HttpObject msg, Session session) {
		Customer customer = session.getCustomer();
		String serviceName = session.getServiceName();

		if ((customer != null) && (serviceName != null)) {
			accountancy.addItem(new AccountancyItem(customer, serviceName, new Date()));
			return msg;
		} else {
			throw new IllegalStateException("Customer or service name is null!");
		}
	}

	/**
	 * Closes the internal resources: accountancy.
	 */
	@Override
	public void close() {
		accountancy.close();
	}

}
