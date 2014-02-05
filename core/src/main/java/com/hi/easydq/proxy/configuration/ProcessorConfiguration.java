package com.hi.easydq.proxy.configuration;

import org.springframework.beans.factory.FactoryBean;

import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;

/**
 * Configuration object for {@link Processor} objects. This is a spring factory
 * bean.
 */
public abstract class ProcessorConfiguration implements FactoryBean<Processor> {

	public abstract Processor createProcessor() throws Exception;

	@Override
	public Class<?> getObjectType() {
		return Processor.class;
	}

	@Override
	public Processor getObject() throws Exception {
		return createProcessor();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
