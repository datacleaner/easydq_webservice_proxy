/**
 * 
 */
package com.hi.easydq.proxy.modules.prepostprocessing;

import io.netty.handler.codec.http.HttpObject;

import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;

/**
 * The processor used to disable any pre and/or postprocessing.
 * 
 * @author Tomasz Guzialek
 *
 */
public class NoOperationProcessor implements Processor {

	/**
	 * Return the request taken as parameter without any processing. 
	 */
	@Override
	public HttpObject process(HttpObject msg, Session session) {
		return msg;
	}

	/**
	 * Does not close anything.
	 */
	@Override
	public void close() {
	}

}
