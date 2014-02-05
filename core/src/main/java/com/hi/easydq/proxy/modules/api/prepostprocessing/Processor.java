package com.hi.easydq.proxy.modules.api.prepostprocessing;

import io.netty.handler.codec.http.HttpObject;

import java.io.Closeable;

import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions.ProcessorException;

/**
 * The interface for pre- and postprocessors of the requests.
 * 
 * @author Tomasz Guzialek
 *
 */
public interface Processor extends Closeable {

	/**
	 * Contains the code to execute by the pre- or postprocessor.
	 * 
	 * @param msg The request being processed.
	 */
	public HttpObject process(HttpObject msg, Session session) throws ProcessorException;

	@Override
	public void close();
}
