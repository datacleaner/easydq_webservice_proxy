package com.hi.easydq.proxy.modules.prepostprocessing.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

import com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions.ProcessorException;

public class NotAuthorizedException extends ProcessorException {

	private static final long serialVersionUID = 2146286172507737904L;

	public NotAuthorizedException(String message) {
		super(message);
	}
	
	public NotAuthorizedException() {
		super();
	}
	
	@Override
	public int getHttpErrorCode() {
		return HttpResponseStatus.UNAUTHORIZED.code();
	}

	@Override
	public String getHttpErrorDescription() {
		return HttpResponseStatus.UNAUTHORIZED.reasonPhrase();
	}

	@Override
	public HttpResponseStatus getHttpResponseStatusObject() {
		return HttpResponseStatus.UNAUTHORIZED;
	}

}
