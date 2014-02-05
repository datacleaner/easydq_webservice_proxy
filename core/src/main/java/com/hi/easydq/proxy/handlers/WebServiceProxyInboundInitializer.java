package com.hi.easydq.proxy.handlers;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.Module;

/**
 * Initializes the channel for communication between the client and the proxy.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class WebServiceProxyInboundInitializer extends
		ChannelInitializer<SocketChannel> {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(WebServiceProxyInboundInitializer.class);

	private final Map<String, Module> modules;

	public WebServiceProxyInboundInitializer(Map<String, Module> modules) {
		this.modules = modules;
	}

	/**
	 * Adds the codecs and handlers to the channel pipeline:
	 * {@link HttpContentDecompressor}, {@link HttpServerCodec},
	 * {@link HttpObjectAggregator} and {@link WebServiceProxyInboundHandler}.
	 * Starts reading on the channel.
	 */
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast("httpContentDecompressor",
				new HttpContentDecompressor());
		ch.pipeline().addLast("httpServerCodec", new HttpServerCodec());
		ch.pipeline().addLast("aggregator",
				new HttpObjectAggregator(512 * 1024));
		ch.pipeline().addLast("handler",
				new WebServiceProxyInboundHandler(modules));

		ch.read();
	}

}
