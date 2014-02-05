package com.hi.easydq.proxy.modules;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.AbstractModule;
import com.hi.easydq.proxy.modules.api.Session;

/**
 * A module that just discards the request that come to the proxy. Sends also 400 Bad Request response.
 * 
 * @author Tomasz Guzialek
 *
 */
public class DiscardingModule extends AbstractModule {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(DiscardingModule.class);


	/* (non-Javadoc)
	 * @see com.hi.easydq.proxy.modules.api.AbstractModule#startSession(io.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public Session startSession(ChannelHandlerContext inboundChannelContext) {
		return new DiscardingSession(inboundChannelContext);
	}


	@Override
	public void close() {
	}
}
