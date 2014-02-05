package com.hi.easydq.proxy.modules.prepostprocessing.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

import com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions.ProcessorException;

public class AuthenticationProviderConnectionException extends
		ProcessorException {

	private static final long serialVersionUID = -213053925504768305L;

	public AuthenticationProviderConnectionException(Exception e) {
		super(e);
	}
	
	public AuthenticationProviderConnectionException(String message) {
		super(message);
	}
	
	public AuthenticationProviderConnectionException() {
		super();
	}
	
	@Override
	public int getHttpErrorCode() {
		return HttpResponseStatus.SERVICE_UNAVAILABLE.code();
	}

	@Override
	public String getHttpErrorDescription() {
		return HttpResponseStatus.SERVICE_UNAVAILABLE.reasonPhrase();
	}

	@Override
	public HttpResponseStatus getHttpResponseStatusObject() {
		return HttpResponseStatus.SERVICE_UNAVAILABLE;
	}

}
