package com.hi.easydq.proxy.modules.prepostprocessing;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions.ProcessorException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.AuthenticationHeaderMissingException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.BadRequestException;
import com.hi.easydq.proxy.users.Customer;
import com.hi.easydq.proxy.util.AuthenticationUtils;

/**
 * The preprocessor which does NOT verify specified credentials, just logs them.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class CustomerLoggingPreProcessor implements Processor {

	/**
	 * Reads the header and extracts the authentication header, if present.
	 * Decode the Byte64-encoded version and stores the credentials for later
	 * use.
	 */
	@Override
	public HttpObject process(HttpObject msg, Session session)
			throws ProcessorException {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			String authenticationHeader = httpRequest.headers().get(
					HttpHeaders.Names.AUTHORIZATION);
			if (authenticationHeader != null) {
				Customer customer = AuthenticationUtils.decodeAuthenticationHeader(authenticationHeader);
				session.setCustomer(customer);
				httpRequest.headers().remove(HttpHeaders.Names.AUTHORIZATION);
				return httpRequest;
			} else {
				throw new AuthenticationHeaderMissingException();
			}
		} else {
			throw new BadRequestException();
		}
	}

	@Override
	public void close() {

	}

}
