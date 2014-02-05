package com.hi.easydq.proxy.modules;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.handlers.WebServiceProxyOutboundInitializer;
import com.hi.easydq.proxy.modules.api.AbstractSession;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.modules.api.prepostprocessing.exceptions.ProcessorException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.AuthenticationHeaderMissingException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.AuthenticationProviderConnectionException;
import com.hi.easydq.proxy.modules.prepostprocessing.exceptions.NotAuthorizedException;
import com.hi.easydq.proxy.util.SessionUtils;
import com.hi.easydq.proxy.util.WebServiceProxyChannelUtility;

/**
 * Represents the session that forwards the request to the backend server and
 * sends back the responses from the backend too.
 * 
 * @author Tomasz Guzialek
 * 
 */
class ForwardingSession extends AbstractSession {

	private static final Logger logger = LoggerFactory
			.getLogger(ForwardingSession.class);

	/**
	 * Reference to the channel between the proxy and the backend.
	 */
	private Channel outboundChannel = null;

	/**
	 * Indicates if the connection to the backend is already established.
	 */
	private final AtomicBoolean outboundConnectionActive;

	/**
	 * Queues the messages until the session is set to active.
	 */
	private final BlockingQueue<HttpObject> messageQueue;

	/**
	 * Responsible for authentication of the customers.
	 */
	private final Processor preProcessor;

	/**
	 * Responsible for accounting usage of the web services by the customers.
	 */
	private final Processor postProcessor;

	/**
	 * Stores the host of the backend server.
	 */
	private final String remoteHost;

	/**
	 * Stores the port of the backend server.
	 */
	private final int remotePort;

	public ForwardingSession(final ChannelHandlerContext inboundChannelContext,
			final String remoteHost, final int remotePort,
			Processor preProcessor, Processor postProcessor) {
		super(inboundChannelContext);
		this.outboundConnectionActive = new AtomicBoolean(false);
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.messageQueue = new LinkedBlockingQueue<HttpObject>();
		this.preProcessor = preProcessor;
		this.postProcessor = postProcessor;
	}

	/**
	 * Pre-processes the request and if successful - adds it to the queue. Next,
	 * checks if the connection to the backend is active. If yes, flushes the
	 * queue. If not, connects to the backend and then the queue is flushed.
	 */
	public void handleHttpRequest(final ChannelHandlerContext ctx,
			HttpObject httpObject) {

		FullHttpRequest preProcessedHttpRequest;
		try {
			preProcessedHttpRequest = (FullHttpRequest) preProcessor.process(
					httpObject, this);
			setServiceNameFromUri(preProcessedHttpRequest.getUri());
			messageQueue.add(preProcessedHttpRequest);
		} catch (ProcessorException e) {
			if (e instanceof AuthenticationProviderConnectionException)
				logger.error(
						"Error while connecting to authentication provider.", e);
			else if (e instanceof AuthenticationHeaderMissingException)
				logger.error(
						"Authentication header is missing in the request!", e);
			else if (e instanceof NotAuthorizedException)
				logger.error("Authentication credentials are incorrect!", e);
			handleHttpResponse(null, prepareErrorResponse(e));
		}

		if ((!outboundConnectionActive.get()) && ((!messageQueue.isEmpty()))) {
			connectToBackend(remoteHost, remotePort);
		} else if (!messageQueue.isEmpty()) {
			flushQueue();
		}

	}

	/**
	 * Sets the service name in the session based on the URI of the request.
	 * 
	 * @param uri The URI of the request.
	 */
	private void setServiceNameFromUri(String uri) {
		// Possible transformations to be implemented in here.
		this.setServiceName(uri);
	}

	/**
	 * Prepares the HTTP response to the client informing about the reason while
	 * request failed.
	 * 
	 * @return HttpResponse with specific error code.
	 */
	private FullHttpResponse prepareErrorResponse(ProcessorException e) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				e.getHttpResponseStatusObject());
	}

	/**
	 * Connects to the backend server and flushes the message queue.
	 * 
	 * @param remoteHost
	 *            The host of the backend server.
	 * @param remotePort
	 *            The port of the backend server.
	 */
	private void connectToBackend(final String remoteHost, final int remotePort) {
		final Channel inboundChannel = inboundChannelContext.channel();

		Bootstrap b = new Bootstrap();
		b.group(inboundChannel.eventLoop())
				.channel(NioSocketChannel.class)
				.handler(
						new WebServiceProxyOutboundInitializer(
								inboundChannelContext))
				.option(ChannelOption.AUTO_READ, true);
		ChannelFuture outboundChannelFuture = b.connect(remoteHost, remotePort);

		outboundChannelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				if (future.isSuccess()) {
					outboundChannel = future.channel();
					// outboundConnectionActive.set(true);
					signalOutboundConnectionActive();
					logger.info("Connection to " + remoteHost + ":"
							+ remotePort + " established.");
					// connection complete start to read first data
					flushQueue();
					inboundChannel.read();
				} else {
					// Close the connection if the connection attempt
					// has
					// failed.
					logger.warn("Connection to " + remoteHost + ":"
							+ remotePort + " failed.", future.cause());
					WebServiceProxyChannelUtility.closeOnFlush(inboundChannel);
				}
			}
		});
	}

	/**
	 * When the session becomes active, all the messages in the queue are
	 * forwarded to the backend.
	 */
	private void flushQueue() {
		HttpObject msg = messageQueue.poll();
		if (msg != null) {
			outboundChannel.writeAndFlush(msg).addListener(
					new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future)
								throws Exception {
							if (future.isSuccess()) {
								logger.debug("HttpObject forwarded succesfully");
								flushQueue();
							} else {
								logger.warn("Writing to outbound channel was unsuccesful, closing channel future");
								WebServiceProxyChannelUtility
										.closeOnFlush(future.channel());
							}
						}
					});
		} else {
			// was able to flush out all the queue, start to read the
			// next chunk
			inboundChannelContext.read();
		}
	}

	/**
	 * Closes the internal resources: outbound channel.
	 */
	public void close() {
		WebServiceProxyChannelUtility.closeOnFlush(outboundChannel);
	}

	/**
	 * Sets the session to active and invokes #flushQueue. Thread-safe.
	 */
	public void signalOutboundConnectionActive() {
		outboundConnectionActive.set(true);
		flushQueue();
	}

	/**
	 * The method to put send the message to the client (and flush). Checks if
	 * the operation was successful. After successful sending invokes a read on
	 * the outbound channel (waiting for the next response messages to handle).
	 * 
	 * @param outboundChannelContext
	 * @param httpObject
	 */
	@Override
	public void handleHttpResponse(
			final ChannelHandlerContext outboundChannelContext,
			HttpObject httpObject) {
		final HttpObject httpObjectFinal = httpObject;

		inboundChannelContext.channel().writeAndFlush(httpObject)
				.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(
							ChannelFuture inboundChannelFuture)
							throws Exception {
						if (inboundChannelFuture.isSuccess()) {
							if (httpObjectFinal instanceof HttpResponse) {
								logger.debug("HttpResponse sent back to the client successfully.");
								HttpResponse httpResponse = (HttpResponse) httpObjectFinal;
								if (httpResponse.getStatus().equals(
										HttpResponseStatus.OK)) {
									postProcessor
											.process(
													httpObjectFinal,
													SessionUtils
															.getSession(inboundChannelContext));
								} else {
									WebServiceProxyChannelUtility
											.closeOnFlush(inboundChannelFuture
													.channel());
								}

							}
						} else {
							logger.warn(
									"The HttpResponse failed to be sent back correctly! Closing the inboundChannel.",
									inboundChannelFuture.cause());
							WebServiceProxyChannelUtility
									.closeOnFlush(inboundChannelFuture
											.channel());
						}
					}
				});

	}

}
