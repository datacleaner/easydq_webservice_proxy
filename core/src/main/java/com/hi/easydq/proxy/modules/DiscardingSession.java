package com.hi.easydq.proxy.modules;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.AbstractSession;
import com.hi.easydq.proxy.util.WebServiceProxyChannelUtility;

/**
 * Represents a session where all the request that come to the proxy are
 * discarded. Error code 400 Bad Request is sent back to the client.
 * 
 * @author Tomasz Guzialek
 * 
 */
class DiscardingSession extends AbstractSession {

	private static final Logger logger = LoggerFactory
			.getLogger(DiscardingSession.class);

	public DiscardingSession(final ChannelHandlerContext inboundChannelContext) {
		super(inboundChannelContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hi.easydq.proxy.modules.api.Session#handleHttpObject(io.netty.channel
	 * .ChannelHandlerContext, io.netty.handler.codec.http.HttpObject)
	 */
	public void handleHttpRequest(final ChannelHandlerContext ctx,
			HttpObject httpObject) {
		this.setServiceName("DiscardingSession");
		logger.error("Discarding request and sending back the error code 400 Bad Request!");
		FullHttpResponse errorResponse = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
		handleHttpResponse(null, errorResponse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() {
		logger.info("Closing inbound channel from DiscardSession object: "
				+ this.hashCode());
		WebServiceProxyChannelUtility.closeOnFlush(inboundChannelContext
				.channel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hi.easydq.proxy.modules.api.Session#handleResponseHttpObject(io.netty
	 * .channel.ChannelHandlerContext, io.netty.handler.codec.http.HttpObject)
	 */
	@Override
	public void handleHttpResponse(ChannelHandlerContext ctx,
			HttpObject httpObject) {
		inboundChannelContext.channel().writeAndFlush(httpObject)
				.addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future)
							throws Exception {
						if (future.isSuccess()) {
							logger.debug("Error code sent back successfully.");
							close();
						} else {
							logger.debug("Sending back error code failed.");
							close();
						}
					}
				});
	}

}
