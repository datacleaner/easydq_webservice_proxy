package com.hi.easydq.proxy.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import com.hi.easydq.proxy.modules.ModuleSelector;
import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.api.Session;

/**
 * Utility class for retrieving session objects.
 * 
 * @author Tomasz Guzialek
 * 
 */
public final class SessionUtils {

	/**
	 * Constant used to fetch the session info from the channel handler context.
	 */
	private static final AttributeKey<Session> ATTRIBUTE_KEY_SESSION = AttributeKey.valueOf(
			"WebProxySession");

	/**
	 * Private constructor preventing instantiation.
	 */
	private SessionUtils() {
		// prevent instantiation
	}

	/**
	 * Fetches the session from the channel handler context. If not found, null is returned.
	 * 
	 * @param ctx Channel handler context to determine session from.
	 * @return Session object or null.
	 */
	public static Session getSession(ChannelHandlerContext ctx) {
		Attribute<Session> attribute = ctx.attr(ATTRIBUTE_KEY_SESSION);
		return attribute.get();
	}

	/**
	 * Checks if the Session object exists in the map in ChannelHandlerContext.
	 * If not, determines the session using {@link ModuleSelector}. Never returns null.
	 * 
	 * @param ctx Channel handler context to determine session from.
	 * @param moduleSelector The object responsible for selecting a module based on the request, if not found in the context.
	 * @param msg The request to determine session from, if not found in the context.
	 * @return The session.
	 */
	public static Session getOrCreateSession(ChannelHandlerContext ctx,
			ModuleSelector moduleSelector, Object msg) {
		Attribute<Session> attribute = ctx.attr(ATTRIBUTE_KEY_SESSION);
		Session session;
		if (attribute.get() != null) {
			session = attribute.get();
		} else {
			Module module = moduleSelector.selectModule(msg);
			session = module.startSession(ctx);
			attribute.set(session);
		}
		return session;
	}
}
