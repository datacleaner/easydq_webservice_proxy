package com.hi.easydq.proxy.modules;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.configuration.ModuleConfiguration;
import com.hi.easydq.proxy.modules.api.AbstractModule;
import com.hi.easydq.proxy.modules.api.Session;
import com.hi.easydq.proxy.modules.prepostprocessing.Accountancy;

/**
 * A module used by administrators to check the state of the proxy application
 * and its modules. Sends back the HTML page with diagnostic information.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class HealthCheckModule extends AbstractModule {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(HealthCheckModule.class);

	/**
	 * Reference to the collection of ModuleConfiguration objects being printed.
	 */
	private final Collection<ModuleConfiguration> modules;
	
	/**
	 * The reference to the object storing billing information for all the modules.  
	 */
	private final Accountancy accountancy;

	public HealthCheckModule(Collection<ModuleConfiguration> modules, Accountancy accountancy) {
		this.modules = modules;
		this.accountancy = accountancy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hi.easydq.proxy.modules.api.AbstractModule#startSession(io.netty.
	 * channel.ChannelHandlerContext)
	 */
	public Session startSession(
			final ChannelHandlerContext inboundChannelContext) {
		return new HealthCheckSession(inboundChannelContext, modules, accountancy);
	}

	@Override
	public void close() {
		accountancy.close();
	}
}
