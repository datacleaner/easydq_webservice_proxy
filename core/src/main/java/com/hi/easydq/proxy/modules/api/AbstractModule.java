package com.hi.easydq.proxy.modules.api;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author Tomasz Guzialek
 * 
 * Abstract base class for the majority of modules. Not used so far.
 *
 */
public abstract class AbstractModule implements Module {

	/* (non-Javadoc)
	 * @see com.hi.easydq.proxy.modules.api.Module#startSession(io.netty.channel.Channel)
	 */
	@Override
	public abstract Session startSession(ChannelHandlerContext inboundChannelContext);

}