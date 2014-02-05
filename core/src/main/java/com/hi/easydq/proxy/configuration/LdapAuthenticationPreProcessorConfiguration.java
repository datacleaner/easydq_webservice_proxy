package com.hi.easydq.proxy.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.modules.prepostprocessing.AuthenticationPreProcessor;
import com.hi.easydq.proxy.users.AuthenticationProvider;
import com.hi.easydq.proxy.users.LdapAuthenticationProvider;

public class LdapAuthenticationPreProcessorConfiguration extends
		ProcessorConfiguration {
	
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(LdapAuthenticationPreProcessorConfiguration.class);

	private String hostname;
	private int port;
	private String username;
	private String password;

	@Override
	public Processor createProcessor() throws Exception {
		AuthenticationProvider authenticationProvider = new LdapAuthenticationProvider(
				hostname, port, username, password);
		authenticationProvider.connect();
		return new AuthenticationPreProcessor(authenticationProvider);
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
