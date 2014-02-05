package com.hi.easydq.proxy.modules.api;

import java.io.Closeable;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface for modules in the web proxy.
 * 
 * @author Tomasz Guzialek
 * 
 */
public interface Module extends Closeable {

	/**
	 * The method for starting the module. Return the new session.
	 * 
	 * @param ctx
	 *            The reference to the channel context in order to send back the
	 *            response.
	 * @return Return the session for current connection.
	 */
	public Session startSession(final ChannelHandlerContext ctx);

	@Override
	public void close();
}