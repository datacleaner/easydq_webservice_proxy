package com.hi.easydq.proxy.modules.prepostprocessing;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

import com.hi.easydq.proxy.modules.DiscardingModule;
import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.users.Customer;

public class AccountancyPostProcessorTest {

	private AccountancyPostProcessor accountancyPostProcessor;

	@Test
	public void testSuccessfulAdding() {
		Accountancy accountancy = EasyMock.createMock(Accountancy.class);
		Module module = new DiscardingModule();
		Session session = module.startSession(null);
		session.setCustomer(new Customer("testUserName", "testPassword"));
		session.setServiceName("testServiceName");
		module.close();

		Capture<AccountancyItem> capture = new Capture<AccountancyItem>();
		
		accountancy.addItem(EasyMock.capture(capture));

		EasyMock.replay(accountancy);

		accountancyPostProcessor = new AccountancyPostProcessor(accountancy);
		accountancyPostProcessor.process(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/testRequestPath"), session);
		
		EasyMock.verify(accountancy);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testCustomerIsNull() {
		Accountancy accountancy = EasyMock.createMock(Accountancy.class);
		Module module = new DiscardingModule();
		Session session = module.startSession(null);
		// The lack of setter invocation for customer
		session.setServiceName("testServiceName");
		module.close();

		EasyMock.replay(accountancy);

		accountancyPostProcessor = new AccountancyPostProcessor(accountancy);
		accountancyPostProcessor.process(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/testRequestPath"), session);
		
		EasyMock.verify(accountancy);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testServiceNameIsNull() {
		Accountancy accountancy = EasyMock.createMock(Accountancy.class);
		Module module = new DiscardingModule();
		Session session = module.startSession(null);
		session.setCustomer(new Customer("testUserName", "testPassword"));
		// The lack of setter invocation for service name
		module.close();

		EasyMock.replay(accountancy);

		accountancyPostProcessor = new AccountancyPostProcessor(accountancy);
		accountancyPostProcessor.process(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/testRequestPath"), session);
		
		EasyMock.verify(accountancy);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testCustomerAndServiceNameAreNull() {
		Accountancy accountancy = EasyMock.createMock(Accountancy.class);
		Module module = new DiscardingModule();
		Session session = module.startSession(null);
		// The lack of setter invocation for customer
		// The lack of setter invocation for service name
		module.close();

		EasyMock.replay(accountancy);

		accountancyPostProcessor = new AccountancyPostProcessor(accountancy);
		accountancyPostProcessor.process(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/testRequestPath"), session);
		
		EasyMock.verify(accountancy);
	}

}
