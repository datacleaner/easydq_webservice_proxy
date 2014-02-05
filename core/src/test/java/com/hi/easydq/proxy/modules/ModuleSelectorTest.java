package com.hi.easydq.proxy.modules;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hi.easydq.proxy.modules.api.Module;
import com.hi.easydq.proxy.modules.prepostprocessing.NoOperationProcessor;

public class ModuleSelectorTest {

	@Test
	public void testSelectModuleDocs() {
		Map<String, Module> modules = new LinkedHashMap<String, Module>();
		ForwardingModule expectedModule = new ForwardingModule("theChosen", 8080, new NoOperationProcessor(), new NoOperationProcessor());
		modules.put("/fake/deeper", new ForwardingModule("notImportant3", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/fake", new ForwardingModule("notImportant2", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs/deeper1", new ForwardingModule("notImportant4", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs/deeper2", new ForwardingModule("notImportant5", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs", expectedModule);
		modules.put("/", new ForwardingModule("notImportant1", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		
		DefaultFullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/docs");
		
		ModuleSelector moduleSelector = new ModuleSelector(modules);
		Module module = moduleSelector.selectModule(httpRequest);
		Assert.assertSame(expectedModule, module);
	}

	@Test
	public void testSelectModuleDocsDeeper1() {
		Map<String, Module> modules = new LinkedHashMap<String, Module>();
		ForwardingModule expectedModule = new ForwardingModule("theChosen", 8080, new NoOperationProcessor(), new NoOperationProcessor());
		modules.put("/fake", new ForwardingModule("notImportant2", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs", new ForwardingModule("notImportant3", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs/deeper1", expectedModule);
		modules.put("/docs/deeper2", new ForwardingModule("notImportant4", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/", new ForwardingModule("notImportant1", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		
		DefaultFullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/docs/deeper1");
		
		ModuleSelector moduleSelector = new ModuleSelector(modules);
		Module module = moduleSelector.selectModule(httpRequest);
		Assert.assertSame(expectedModule, module);
	}
	
	@Test
	public void testSelectModuleNoMatch() {
		Map<String, Module> modules = new LinkedHashMap<String, Module>();
		modules.put("/fake", new ForwardingModule("notImportant2", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs/deeper1", new ForwardingModule("notImportant", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs/deeper2", new ForwardingModule("notImportant4", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		modules.put("/docs/", new ForwardingModule("notImportant3", 8080, new NoOperationProcessor(), new NoOperationProcessor()));
		
		DefaultFullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/notExistingPath");
		
		ModuleSelector moduleSelector = new ModuleSelector(modules);
		Module module = moduleSelector.selectModule(httpRequest);
		Assert.assertTrue(module instanceof DiscardingModule);
	}
	
}
