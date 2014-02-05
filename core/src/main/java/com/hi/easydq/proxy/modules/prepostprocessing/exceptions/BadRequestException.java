package com.hi.easydq.proxy.modules.prepostprocessing.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

import com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions.ProcessorException;

public class BadRequestException extends ProcessorException {

	private static final long serialVersionUID = -269454708014339145L;

	public BadRequestException(String message) {
		super(message);
	}
	
	public BadRequestException() {
		super();
	}
	
	@Override
	public int getHttpErrorCode() {
		return HttpResponseStatus.BAD_REQUEST.code();
	}

	@Override
	public String getHttpErrorDescription() {
		return HttpResponseStatus.BAD_REQUEST.reasonPhrase();
	}

	@Override
	public HttpResponseStatus getHttpResponseStatusObject() {
		return HttpResponseStatus.BAD_REQUEST;
	}

}
