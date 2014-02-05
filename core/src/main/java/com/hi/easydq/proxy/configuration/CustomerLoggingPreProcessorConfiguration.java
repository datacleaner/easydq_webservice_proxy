package com.hi.easydq.proxy.configuration;

import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.modules.prepostprocessing.CustomerLoggingPreProcessor;

public class CustomerLoggingPreProcessorConfiguration extends ProcessorConfiguration {

	@Override
	public Processor createProcessor() {
		return new CustomerLoggingPreProcessor();
	}

}
