package com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class ProcessorException extends Exception {

	private static final long serialVersionUID = 287681171943913303L;
	
	public ProcessorException(String message) {
		super(message);
	}
	
	public ProcessorException() {
		super();
	}
	
	public ProcessorException(Exception e) {
		super(e);
	}

	/**
	 * Returns the HttpResponseStatus associated with this exception.
	 * 
	 * @return The HttpResponseStatus object
	 */
	public abstract HttpResponseStatus getHttpResponseStatusObject();
	
	/**
	 * Returns the HTTP error code associated with this exception.
	 * 
	 * @return The code.
	 */
	public abstract int getHttpErrorCode();
	
	/**
	 * Returns the human-readable description of the HTTP error associated with this exception.
	 * 
	 * @return The description.
	 */
	public abstract String getHttpErrorDescription();

}
