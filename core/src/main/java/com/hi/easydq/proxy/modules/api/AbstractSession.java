package com.hi.easydq.proxy.modules.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;

import java.io.IOException;

import com.hi.easydq.proxy.users.Customer;

public abstract class AbstractSession implements Session {

	private Customer customer;
	private String serviceName;
	
	/**
	 * Reference to the channel context in order to send back the responses.
	 */
	protected final ChannelHandlerContext inboundChannelContext;

	public AbstractSession(ChannelHandlerContext inboundChannelContext) {
		this.inboundChannelContext = inboundChannelContext;
	}

	@Override
	public Customer getCustomer() {
		return customer;
	}

	@Override
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;

	}

	@Override
	public abstract void close() throws IOException;

	@Override
	public abstract void handleHttpRequest(ChannelHandlerContext ctx,
			HttpObject httpObject);

	@Override
	public abstract void handleHttpResponse(ChannelHandlerContext ctx,
			HttpObject httpObject);

}
