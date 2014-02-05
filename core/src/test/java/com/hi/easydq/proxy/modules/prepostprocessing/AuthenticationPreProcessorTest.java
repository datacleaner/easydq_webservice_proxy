package com.hi.easydq.proxy.modules.prepostprocessing;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.commons.codec.binary.Base64;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.hi.easydq.proxy.modules.DiscardingModule;
import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.AuthenticationHeaderMissingException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.NotAuthorizedException;
import com.hi.easydq.proxy.users.AuthenticationProvider;
import com.hi.easydq.proxy.users.Customer;

public class AuthenticationPreProcessorTest {

	@Test
	public void testAuthenticationSuccess() throws Exception {
		AuthenticationProvider authenticationProvider = new MockAuthenticationProvider();

		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.GET, "testUri");
		String userName = "validUser";
		String password = "validPassword";
		String decodedValue =  userName + ":" + password;
		String encodedValue = new String("Basic " + Base64.encodeBase64String(decodedValue.getBytes()));
		System.out.println(encodedValue);
		httpRequest.headers().add(HttpHeaders.Names.AUTHORIZATION, encodedValue);

		Module module = new DiscardingModule();
		Session session = module.startSession(null);
		session.setCustomer(new Customer("testUserName", "testPassword"));
		session.setServiceName("testServiceName");
		module.close();
		
		@SuppressWarnings("resource")
		// close() method invokes only authenticationProvider.close(), which has been invoked explicitly above.
		AuthenticationPreProcessor authenticationPreProcessor = new AuthenticationPreProcessor(
				authenticationProvider);
		
		HttpRequest processedRequest = (HttpRequest) authenticationPreProcessor.process(httpRequest, session);
		
		Assert.assertNull(processedRequest.headers().get(HttpHeaders.Names.AUTHORIZATION));
	}
	
	@Test(expected = AuthenticationHeaderMissingException.class)
	public void testAuthenticationHeaderMissing() throws Exception {
		AuthenticationProvider authenticationProvider = EasyMock
				.createMock(AuthenticationProvider.class);

		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.GET, "testUri");

		Module module = new DiscardingModule();
		Session session = module.startSession(null);
		session.setCustomer(new Customer("testUserName", "testPassword"));
		session.setServiceName("testServiceName");
		module.close();

		EasyMock.replay(authenticationProvider);
		
		@SuppressWarnings("resource")
		// close() method invokes only authenticationProvider.close(), which has been invoked explicitly above.
		AuthenticationPreProcessor authenticationPreProcessor = new AuthenticationPreProcessor(
				authenticationProvider);
		authenticationPreProcessor.process(httpRequest, session);

		EasyMock.verify(authenticationProvider);
		
	}
	
	@Test(expected = NotAuthorizedException.class)
	public void testAuthenticationFailure() throws Exception {
		AuthenticationProvider authenticationProvider = EasyMock
				.createMock(AuthenticationProvider.class);

		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.GET, "testUri");
		String userName = "inexistentUser";
		String password = "testPassword";
		String decodedValue =  userName + ":" + password;
		String encodedValue = new String("Basic " + Base64.encodeBase64String(decodedValue.getBytes()));
		System.out.println(encodedValue);
		httpRequest.headers().add(HttpHeaders.Names.AUTHORIZATION, encodedValue);

		Module module = new DiscardingModule();
		Session session = module.startSession(null);
		session.setCustomer(new Customer("inexistentUser", "testPassword"));
		session.setServiceName("testServiceName");
		module.close();

		authenticationProvider.connect();
		EasyMock.expect(authenticationProvider.authenticate(userName, password)).andReturn(false);
		authenticationProvider.close();

		EasyMock.replay(authenticationProvider);
		
		@SuppressWarnings("resource")
		// close() method invokes only authenticationProvider.close(), which has been invoked explicitly above.
		AuthenticationPreProcessor authenticationPreProcessor = new AuthenticationPreProcessor(
				authenticationProvider);
		authenticationPreProcessor.process(httpRequest, session);

		EasyMock.verify(authenticationProvider);
		
	}
	
	private class MockAuthenticationProvider implements AuthenticationProvider {

		@Override
		public void connect() throws Exception {
			// Do nothing.
		}

		@Override
		public boolean authenticate(String userName, String password)
				throws Exception {
			if (userName.equals("validUser") && (password.equals("validPassword")))
				return true;
			else
				return false;
		}

		@Override
		public void close() {
			// Do nothing.
		}
		
		
	}
	
	

}
