package com.hi.easydq.proxy.modules;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.configuration.ModuleConfiguration;
import com.hi.easydq.proxy.handlers.WebServiceProxyInboundHandler;
import com.hi.easydq.proxy.handlers.WebServiceProxyOutboundHandler;
import com.hi.easydq.proxy.modules.api.AbstractSession;
import com.hi.easydq.proxy.modules.prepostprocessing.Accountancy;
import com.hi.easydq.proxy.modules.prepostprocessing.AccountancyItem;
import com.hi.easydq.proxy.util.WebServiceProxyChannelUtility;

/**
 * Represents the session of the {@link HealthCheckModule}. Sends back the HTML
 * page with diagnostic information.
 * 
 * @author Tomasz Guzialek
 * 
 */
class HealthCheckSession extends AbstractSession {

	private static final Logger logger = LoggerFactory
			.getLogger(HealthCheckSession.class);

	/**
	 * Reference to the modules map in order to determine the modules added.
	 */
	private final Collection<ModuleConfiguration> modules;

	/**
	 * The reference to the object storing billing information for all the
	 * modules.
	 */
	private final Accountancy accountancy;

	public HealthCheckSession(
			final ChannelHandlerContext inboundChannelContext,
			Collection<ModuleConfiguration> modules, Accountancy accountancy) {
		super(inboundChannelContext);
		this.modules = modules;
		this.accountancy = accountancy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hi.easydq.proxy.modules.api.Session#handleHttpObject(io.netty.channel
	 * .ChannelHandlerContext, io.netty.handler.codec.http.HttpObject)
	 */
	@Override
	public void handleHttpRequest(ChannelHandlerContext ctx,
			HttpObject httpObject) {
		this.setServiceName("HealthCheckSession");
		DefaultFullHttpResponse healthCheckResponse = prepareResponse();
		handleHttpResponse(null, healthCheckResponse);
	}

	/**
	 * Prepares the {@link DefaultHttpResponse} with diagnostic information:
	 * <ul>
	 * <li>Indicates the status of the WebServiceProxy application as OK.</li>
	 * <li>Prints the number of currently active inbound channels.</li>
	 * <li>Prints the number of currently active outboud channels.</li>
	 * <li>Prints the modules added.</li>
	 * </ul>
	 * 
	 * @return DefaultHttpResponse with the report.
	 */
	private DefaultFullHttpResponse prepareResponse() {
		DefaultFullHttpResponse healthCheckResponse = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		healthCheckResponse.headers().set("content-type",
				"text/html; charset=UTF-8");

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<!DOCTYPE html>\r\n");
		strBuilder.append("<html><head><title>");
		strBuilder.append("WebServiceProxy HealthCheck report.");
		strBuilder.append("</title></head><body>\r\n");
		strBuilder.append("HealthCheck status: OK!<br />");
		strBuilder.append("Inbound channels active (including this one): "
				+ WebServiceProxyInboundHandler.getInboundChannelCounter()
				+ "<br />");
		strBuilder.append("Outbound channels active: "
				+ WebServiceProxyOutboundHandler.getOutboundChannelCounter()
				+ "<br />");

		// Module table

		strBuilder.append("Modules:<br />\r\n");
		strBuilder
				.append("<table style=\"border: 1px solid black;\"><tr><td>Path</td><td>Module</td></tr>");
		for (ModuleConfiguration module : modules) {
			strBuilder.append("<tr>");
			strBuilder.append("<td>" + module.getPath() + "</td>\r\n");
			strBuilder.append("<td>" + module + "</td>\r\n");
			strBuilder.append("</tr>");
		}
		strBuilder.append("</table></body></html>\r\n");

		// Accountancy table

		strBuilder.append("Accountancy items:<br />\r\n");
		strBuilder
				.append("<table style=\"border: 1px solid black;\"><tr><td>Customer</td><td>Service name</td><td>Units used</td></tr>");
		for (AccountancyItem entry : accountancy.getAccountancyItems()) {
			strBuilder.append("<tr>");
			strBuilder.append("<td>" + entry.getCustomer().getUsername()
					+ "</td>\r\n");
			strBuilder.append("<td>" + entry.getServiceName() + "</td>\r\n");
			strBuilder.append("<td>" + entry.getTimeStamp() + "</td>\r\n");
			strBuilder.append("</tr>");
		}
		strBuilder.append("</table></body></html>\r\n");

		ByteBuf buffer = Unpooled.copiedBuffer(strBuilder, CharsetUtil.UTF_8);
		healthCheckResponse.content().writeBytes(buffer);
		buffer.release();
		return healthCheckResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		logger.info("Closing inbound channel in HealthCheckSession: "
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
							logger.debug("HealthCheck sent back successfully.");
							close();
						} else {
							logger.warn(
									"HealtCheck failed to be sent back! {}",
									future.cause());
							close();
						}
					}
				});
	}

}
