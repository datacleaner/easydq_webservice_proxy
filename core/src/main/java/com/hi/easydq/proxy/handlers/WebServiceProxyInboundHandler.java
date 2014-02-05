package com.hi.easydq.proxy.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.ModuleSelector;
import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.util.SessionUtils;
import com.hi.easydq.proxy.util.WebServiceProxyChannelUtility;

/**
 * Contains the logic for handling channel events (message received etc.)
 * between a client and the proxy.
 * 
 * @author Tomasz Guzialek
 */
public class WebServiceProxyInboundHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(WebServiceProxyInboundHandler.class);

	/**
	 * A group for storing active inbound channels. Enables bulk operation on
	 * the channels.
	 */
	private static volatile ChannelGroup inboundChannels = new DefaultChannelGroup(
			"inboundChannels", ImmediateEventExecutor.INSTANCE);

	/**
	 * Instance of ModuleSelector class used for determining where to forward
	 * the request.
	 */
	private final ModuleSelector moduleSelector;

	public WebServiceProxyInboundHandler(Map<String, Module> modules) {
		this.moduleSelector = new ModuleSelector(modules);
	}

	/**
	 * Invoked when the channel gets active. The channel is added to the inbound
	 * channel group for enabling bulk operations on the channels.
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		inboundChannels.add(ctx.channel());
	}

	/**
	 * Checks what type of message has been received and forwards the message to
	 * server determined by @see
	 * #determineSession(io.netty.channel.ChannelHandlerContext,
	 * java.lang.Object) method.
	 */
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {

		Session session = SessionUtils.getOrCreateSession(ctx, moduleSelector,
				msg);

		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			logger.info("(Session hashCode: " + session.hashCode()
					+ ") FullHttpRequest received ({}).", httpRequest.getUri());
			logger.debug("FullHttpRequest: " + httpRequest);
			session.handleHttpRequest(ctx, httpRequest);
		} else {
			logger.warn("Message (unhandled type) received! Not forwarded! "
					+ msg);
		}
	}

	/**
	 * 
	 * Logs the exception and closes the channel.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error("Caught unexpected error", cause);
		WebServiceProxyChannelUtility.closeOnFlush(ctx.channel());
	}

	/**
	 * Gets the current number of active inbound channels.
	 * 
	 * @return The number of active inbound channels.
	 */
	public static int getInboundChannelCounter() {
		return inboundChannels.size();
	}

}
