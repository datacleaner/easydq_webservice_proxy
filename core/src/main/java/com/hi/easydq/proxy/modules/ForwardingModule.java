package com.hi.easydq.proxy.modules;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.AbstractModule;
import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.modules.prepostprocessing.NoOperationProcessor;

/**
 * @author Tomasz Guzialek
 * 
 *         The module used for forwarding requests to specified host and port.
 * 
 */
public class ForwardingModule extends AbstractModule {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(ForwardingModule.class);

	/**
	 * Stores the host of the backend server.
	 */
	private final String remoteHost;

	/**
	 * Stores the port of the backend server.
	 */
	private final int remotePort;
	private final Processor preProcessor;
	private final Processor postProcessor;

	public ForwardingModule(String remoteHost, Integer remotePort,
			Processor preProcessor, Processor postProcessor) {
		if (remoteHost == null || remotePort == null) {
			throw new IllegalArgumentException(
					"Remote host and port cannot be null");
		}
		if (preProcessor == null) {
			preProcessor = new NoOperationProcessor();
		}
		if (postProcessor == null) {
			postProcessor = new NoOperationProcessor();
		}

		this.remoteHost = remoteHost;
		this.remotePort = remotePort;

		this.preProcessor = preProcessor;
		this.postProcessor = postProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hi.easydq.proxy.modules.api.AbstractModule#startSession(io.netty.
	 * channel.Channel)
	 * 
	 * Establishes a connection to the backend server. Checks if the operation
	 * suceeded. Waits for messages to be read from the channel if successful,
	 * closes the connection otherwise.
	 */
	public Session startSession(
			final ChannelHandlerContext inboundChannelContext) {

		return new ForwardingSession(inboundChannelContext, remoteHost,
				remotePort, preProcessor, postProcessor);
	}

	@Override
	public void close() {
		preProcessor.close();
		postProcessor.close();
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(super.toString());
		stringBuilder.append(" = { host: ");
		stringBuilder.append(remoteHost);
		stringBuilder.append(", port: ");
		stringBuilder.append(remotePort);
		stringBuilder.append(", preProcessor: ");
		stringBuilder.append(preProcessor.toString());
		stringBuilder.append(", postProcessor: ");
		stringBuilder.append(postProcessor.toString());
		stringBuilder.append("}");
		return stringBuilder.toString();
	}
}
