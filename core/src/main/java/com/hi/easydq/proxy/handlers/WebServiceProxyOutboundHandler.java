package com.hi.easydq.proxy.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.concurrent.ImmediateEventExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.util.SessionUtils;
import com.hi.easydq.proxy.util.WebServiceProxyChannelUtility;

/**
 * Contains the logic for handling channel events (message received etc.)
 * between the proxy and the backend server.
 * 
 * @author Tomasz Guzialek
 */
public class WebServiceProxyOutboundHandler extends
		ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(WebServiceProxyOutboundHandler.class);

	/**
	 * A group for outbound channels. Enables bulk operation on channels.
	 */
	protected static volatile ChannelGroup outboundChannels = new DefaultChannelGroup(
			"outboundChannels", ImmediateEventExecutor.INSTANCE);

	/**
	 * Reference to the corresponding inbound channel context in order to send
	 * back the response.
	 */
	private final ChannelHandlerContext inboundChannelContext;

	public WebServiceProxyOutboundHandler(
			ChannelHandlerContext inboundChannelContext) {
		this.inboundChannelContext = inboundChannelContext;
	}

	/**
	 * Adds the channel to the outbound channels group to enable bulk operations
	 * on the channels. Ensures the channels is waiting for the messages to come
	 * (listening).
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		outboundChannels.add(ctx.channel());
		ctx.read();
		ctx.write(Unpooled.EMPTY_BUFFER);
	}

	/**
	 * Invoked when a new HTTP response is read. Forwards the response back to the client (byt the session object).
	 */
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Session session = SessionUtils.getSession(inboundChannelContext);

		if (msg instanceof HttpResponse) {
			HttpResponse httpResponse = (HttpResponse) msg;
			logger.info("HttpResponse received.");
			session.handleHttpResponse(ctx, httpResponse);
			logger.debug("HttpResponse: " + httpResponse);
		} else {
			logger.warn("Message (unhandled type) received! Not sending back! "
					+ msg);
		}
	}

	/**
	 * Logs all the exceptions caught and closes the channel. 
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.warn("Exception caught: {}", cause);
		WebServiceProxyChannelUtility.closeOnFlush(ctx.channel());
	}

	/**
	 * Gets the current number of active outbound channels.
	 * 
	 * @return The current number of active outbound channels.
	 */
	public static int getOutboundChannelCounter() {
		return outboundChannels.size();
	}

}
