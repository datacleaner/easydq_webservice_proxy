package com.hi.easydq.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.hi.easydq.proxy.configuration.ModuleConfiguration;
import com.hi.easydq.proxy.configuration.WebProxyConfiguration;
import com.hi.easydq.proxy.handlers.WebServiceProxyInboundInitializer;
import com.hi.easydq.proxy.modules.ForwardingModule;
import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.util.WebServiceProxyChannelUtility;

/**
 * The main class for the web proxy.
 * 
 * Usage from the command line: > java -jar
 * WebServiceProxy-{version}-jar-with-dependencies.jar path/to/configuration.xml
 * 
 * 
 * @author Tomasz Guzialek
 * 
 */
public class WebServiceProxy {

	private static final Logger logger = LoggerFactory
			.getLogger(WebServiceProxy.class);

	/**
	 * Holds the thread pool used for listening and spawning new client
	 * connections.
	 */
	private EventLoopGroup bossGroup;
	/**
	 * Holds the thread pool used for handling already spawned connections.
	 */
	private EventLoopGroup workerGroup;
	/**
	 * Represents the connection with the client of the proxy.
	 */
	private Channel inboundChannel;

	/**
	 * Stores the modules initialized for the web proxy application. Examples:
	 * <ul>
	 * <li>/AddressesWS new ForwardingModule("localhost", 8080)</li>
	 * <li>/EmailsWS new ForwardingModule("localhost", 8181)</li>
	 * <li>/ new ForwardingModule("someServer.com", 80)</li>
	 * </ul>
	 */
	private final Map<String, Module> modules = new LinkedHashMap<String, Module>();

	private final WebProxyConfiguration webProxyConfiguration;

	/**
	 * Initializes the web service proxy based on the path to the configuration
	 * file.
	 * 
	 * @param configurationFilePath
	 *            The path to the configuration file.
	 */
	public WebServiceProxy(String configurationFilePath) {
		this(new File(configurationFilePath));
	}

	/**
	 * Initializes the web service proxy based on the handle to the File.
	 * 
	 * @param configurationFile
	 *            The File handle to the configuration file.
	 */
	public WebServiceProxy(File configurationFile) {
		String path = configurationFile.getAbsolutePath();
		FileSystemXmlApplicationContext applicationContext = new FileSystemXmlApplicationContext(
				"file:" + path);

		try {
			this.webProxyConfiguration = applicationContext
					.getBean(WebProxyConfiguration.class);

			Map<String, ModuleConfiguration> moduleConfigurations = applicationContext
					.getBeansOfType(ModuleConfiguration.class);

			initializeModules(moduleConfigurations.values());
		} finally {
			applicationContext.close();
		}
	}

	/**
	 * Initializes the web service proxy based on the WebProxyConfiguration
	 * object and ModuleConfiguration objects.
	 * 
	 * @param webProxyConfiguration
	 *            The WebProxyConfiguration object.
	 * @param moduleConfigurations
	 *            The list of ModuleConfiguration objects.
	 */
	public WebServiceProxy(WebProxyConfiguration webProxyConfiguration,
			List<ModuleConfiguration> moduleConfigurations) {
		this.webProxyConfiguration = webProxyConfiguration;

		initializeModules(moduleConfigurations);
	}

	private void initializeModules(
			Collection<ModuleConfiguration> moduleConfigurations) {
		for (ModuleConfiguration moduleConfiguration : moduleConfigurations) {
			addModule(moduleConfiguration);
		}
	}

	/**
	 * Starts the proxy and waits for clients to connect. Should be invoked
	 * after adding all the modules to the proxy.
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {
		final int port = webProxyConfiguration.getPort();
		logger.info("WebServiceProxy started at localhost:" + port);

		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new WebServiceProxyInboundInitializer(modules))
				.childOption(ChannelOption.AUTO_READ, false);
		logger.info("Waiting for the client to start a connection...");
		inboundChannel = b.bind(port).sync().channel();
	}

	/**
	 * Shutdown method for the proxy.
	 */
	public void stop() {
		WebServiceProxyChannelUtility.closeOnFlush(inboundChannel);

		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();

		Collection<Module> values = modules.values();
		for (Module module : values) {
			try {
				module.close();
			} catch (Exception e) {
				logger.warn("Failed to close module: " + module, e);
			}
		}
	}

	/**
	 * The method to add modules to the proxy after invoking constructor, but
	 * before run() method.
	 * 
	 * @param moduleConfiguration
	 *            The ModuleConfiguration object.
	 */
	public void addModule(ModuleConfiguration moduleConfiguration) {
		String path = moduleConfiguration.getPath();
		Module module = moduleConfiguration.createModule();

		addModule(path, module);
	}

	/**
	 * The method to add modules to the proxy after invoking constructor, but
	 * before run() method.
	 * 
	 * @param path
	 *            Based on the path the request will be redirected to given
	 *            locations.
	 * @param module
	 *            The Module object.
	 */
	public void addModule(String path, Module module) {
		modules.put(path, module);
	}

	/**
	 * The method to add ForwardingModules to the proxy after invoking
	 * constructor, but before run() method.
	 * 
	 * @param path
	 *            Based on the path the request will be redirected to given
	 *            locations.
	 * @param remoteHost
	 *            The host to forward matching requests.
	 * @param remotePort
	 *            The port to forward matching requests.
	 * @param preProcessor
	 *            The processor executed before forwarding the request.
	 * @param postProcessor
	 *            The processor executed while the response has been
	 *            successfully forwarded back.
	 */
	public void addForwardingModule(String path, String remoteHost,
			int remotePort, Processor preProcessor, Processor postProcessor) {
		modules.put(path, new ForwardingModule(remoteHost, remotePort,
				preProcessor, postProcessor));
	}

	/**
	 * Main method. Takes a path to configuration file as an argument - if not
	 * specified takes default value ("configuration.xml" in current directory).
	 * The path can be an absolute of relative (to current directory) value.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String configurationPath;
		if (args.length < 1) {
			logger.warn("Path to configuration file not specified. Using default path: \"configuration.xml\" file in current directory.");
			configurationPath = "configuration.xml";
		} else {
			configurationPath = args[0];
			logger.info("Using " + configurationPath
					+ " to read configuration.");
		}

		WebServiceProxy webServiceProxy = new WebServiceProxy(configurationPath);

		webServiceProxy.run();
	}
}
