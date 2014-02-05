package com.hi.easydq.proxy.configuration;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hi.easydq.proxy.modules.HealthCheckModule;
import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.prepostprocessing.Accountancy;

public class HealthCheckModuleConfiguration implements ModuleConfiguration, ApplicationContextAware {

	private String path;
	private Accountancy accountancy;
	private ApplicationContext applicationContext;

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Module createModule() {
		Map<String, ModuleConfiguration> moduleConfigurations = applicationContext.getBeansOfType(ModuleConfiguration.class);
		Collection<ModuleConfiguration> modules = moduleConfigurations.values();
		return new HealthCheckModule(modules, accountancy);
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setAccountancy(Accountancy accountancy) {
		this.accountancy = accountancy;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
