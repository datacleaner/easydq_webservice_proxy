package com.hi.easydq.proxy.configuration;

import com.hi.easydq.proxy.modules.api.Module;

public interface ModuleConfiguration {

	public String getPath();
	
	public Module createModule();
}
