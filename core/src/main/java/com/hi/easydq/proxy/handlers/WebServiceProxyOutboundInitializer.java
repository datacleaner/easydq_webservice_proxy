package com.hi.easydq.proxy.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * Initializes the channel for communication between the proxy and the backend
 * (server).
 * 
 * @author Tomasz Guzialek
 */
public class WebServiceProxyOutboundInitializer extends
		ChannelInitializer<SocketChannel> {

	/**
	 * Stores the channel context used for communicating between the client and
	 * proxy in order to send back the response.
	 */
	private volatile ChannelHandlerContext inboundChannelContext;

	public WebServiceProxyOutboundInitializer(
			ChannelHandlerContext inboundChannelContext) {
		this.inboundChannelContext = inboundChannelContext;
	}

	/**
	 * Initializes the outbound channel. Adds the codecs and handlers to the
	 * channel pipeline: {@link HttpContentDecompressor},
	 * {@link HttpClientCodec}, {@link HttpObjectAggregator} and
	 * {@link WebServiceProxyOutboundHandler}.
	 */
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast("httpContentDecompressor",
				new HttpContentDecompressor());
		ch.pipeline().addLast("httpClientCodec", new HttpClientCodec());
		ch.pipeline().addLast("aggregator",
				new HttpObjectAggregator(512 * 1024));
		ch.pipeline().addLast("handler",
				new WebServiceProxyOutboundHandler(inboundChannelContext));
	}

}
