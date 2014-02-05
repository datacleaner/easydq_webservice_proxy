package com.hi.easydq.proxy.modules.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;

import java.io.Closeable;

import com.hi.easydq.proxy.users.Customer;

/**
 * The class representing single connection between the proxy and the backend
 * server.
 * 
 * @author Tomasz Guzialek
 * 
 */
public interface Session extends Closeable {

	/**
	 * Handles the HTTP messages sent by the client to the proxy.
	 * 
	 * @param ctx
	 *            The context of the inbound channel
	 * @param httpObject
	 *            The message from the client.
	 */
	public void handleHttpRequest(final ChannelHandlerContext ctx,
			HttpObject httpObject);

	/**
	 * Handles the HTTP messages that should be sent back to the client.
	 * 
	 * @param ctx
	 *            The context of the outbound channel.
	 * @param httpObject
	 *            The response to the client.
	 */
	public void handleHttpResponse(final ChannelHandlerContext ctx,
			HttpObject httpObject);
	
	/**
	 * @return The customer object associated with the session.
	 */
	public Customer getCustomer();
	
	/**
	 * Sets the customer associated with the session.
	 * 
	 * @param customer
	 */
	public void setCustomer(Customer customer);
	
	/**
	 * @return The service name associated with the session.
	 */
	public String getServiceName();
	
	/**
	 * Sets the service name associated with the session.
	 */
	public void setServiceName(String serviceName);

}
