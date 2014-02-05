package com.hi.easydq.proxy.configuration;

public class WebProxyConfigurationImpl implements WebProxyConfiguration {

	private int port = 8181;

	@Override
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
