package com.hi.easydq.proxy.modules;

import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.modules.api.Module;

/**
 * @author Tomasz Guzialek
 * 
 *         Determines which of the registered modules to use while handling a
 *         request. The path from the request needs to perfectly match the path
 *         from the module (no wildcards, no longest matching approach).
 *         Otherwise default module (DiscardingModule) is returned.
 * 
 */
public class ModuleSelector {

	private static final Logger logger = LoggerFactory
			.getLogger(ModuleSelector.class);

	/**
	 * The map of registered modules. Key: the URI, value: the module itself.
	 */
	private final Map<String, Module> modules;

	public ModuleSelector(Map<String, Module> modules) {
		this.modules = modules;
	};

	/**
	 * Determines the module based on the path from the {@link HttpRequest}.
	 * 
	 * @param msg
	 *            The HttpRequest.
	 * @return The appropriate module.
	 */
	public Module selectModule(Object msg) {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			logger.debug("Selecting module based on the request path: "
					+ httpRequest.getUri());
			String path = httpRequest.getUri();
			Module selectedModule = selectModuleFromPath(path);
			logger.debug("Selected module: " + selectedModule);
			return selectedModule;
		} else {
			throw new UnsupportedOperationException(
					"Cannot determine forwarding module from a non-HttpRequest message: "
							+ msg);
		}
	}

	/**
	 * Uses regular expressions to find a match between the URI from the request
	 * and the URI patterns in the Map.
	 * 
	 * @param path
	 *            Path from find a module for.
	 * @return The appropriate module.
	 */
	private Module selectModuleFromPath(String path) {

		Module module = modules.get(path);
		if (module != null)
			return module;
		else
			return new DiscardingModule();
	}

}
