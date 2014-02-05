package com.hi.easydq.proxy.modules.prepostprocessing.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

import com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions.ProcessorException;

public class AuthenticationHeaderMissingException extends ProcessorException {

	private static final long serialVersionUID = -6938361712091076571L;
	
	public AuthenticationHeaderMissingException(String message) {
		super(message);
	}
	
	public AuthenticationHeaderMissingException() {
		super("");
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
