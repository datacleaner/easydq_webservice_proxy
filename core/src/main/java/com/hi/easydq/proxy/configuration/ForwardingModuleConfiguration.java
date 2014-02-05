package com.hi.easydq.proxy.configuration;

import com.hi.easydq.proxy.modules.ForwardingModule;
import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;

/**
 * Configuration class used by spring to hold the configuration of a single
 * forwarding module.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class ForwardingModuleConfiguration implements ModuleConfiguration {

	private String hostname;
	private int port;
	private String path;
	private Processor preProcessor;
	private Processor postProcessor;

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Module createModule() {
		return new ForwardingModule(hostname, port, preProcessor, postProcessor);
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPostProcessor(Processor postProcessor) {
		this.postProcessor = postProcessor;
	}

	public void setPreProcessor(Processor preProcessor) {
		this.preProcessor = preProcessor;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(super.toString());
		stringBuilder.append(" = { host: ");
		stringBuilder.append(hostname);
		stringBuilder.append(", port: ");
		stringBuilder.append(port);
		stringBuilder.append(", preProcessor: ");
		stringBuilder.append(preProcessor.toString());
		stringBuilder.append(", postProcessor: ");
		stringBuilder.append(postProcessor.toString());
		stringBuilder.append("}");
		return stringBuilder.toString();
	}
}
