package com.hi.easydq.proxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.configuration.ForwardingModuleConfiguration;
import com.hi.easydq.proxy.configuration.ModuleConfiguration;
import com.hi.easydq.proxy.configuration.WebProxyConfigurationImpl;
import com.hi.easydq.proxy.modules.prepostprocessing.NoOperationProcessor;

public class WebServiceProxyTest {

	private static Logger logger = LoggerFactory
			.getLogger(WebServiceProxyTest.class);

	private static final int jettyPort = 8282;
	private static Server server;
	private static WebServiceProxy webServiceProxy;

	@After
	public void tearDown() throws Exception {
		webServiceProxy.stop();
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	@Test(timeout = 10000)
	public void testJetty() throws Exception {
		server = new Server(jettyPort);
		ServletContextHandler servletContextHandler = new ServletContextHandler(
				server, "/", ServletContextHandler.SESSIONS);
		servletContextHandler.addServlet(MockHttpServerServlet.class, "/*");

		server.start();
		logger.info("Jetty started at port " + jettyPort);

		List<ModuleConfiguration> modules = new ArrayList<ModuleConfiguration>();
		ForwardingModuleConfiguration forwardingModuleConfiguration = new ForwardingModuleConfiguration();
		forwardingModuleConfiguration.setPath("/");
		forwardingModuleConfiguration.setHostname("localhost");
		forwardingModuleConfiguration.setPort(jettyPort);
		forwardingModuleConfiguration
				.setPreProcessor(new NoOperationProcessor());
		forwardingModuleConfiguration
				.setPostProcessor(new NoOperationProcessor());
		modules.add(forwardingModuleConfiguration);
		
		final WebProxyConfigurationImpl webProxyConfiguration = new WebProxyConfigurationImpl();
		webProxyConfiguration.setPort(8181);
		
		webServiceProxy = new WebServiceProxy(webProxyConfiguration, modules);
		webServiceProxy.run();

		HttpHost targetHost = new HttpHost("localhost",
				webProxyConfiguration.getPort(), "http");

		CloseableHttpClient httpClient = HttpClients.custom().build();

		HttpGet httpGet = new HttpGet("/");

		CloseableHttpResponse resp = httpClient.execute(targetHost, httpGet);
		String strResponse = EntityUtils.toString(resp.getEntity());
		resp.close();
		httpClient.close();

		Assert.assertEquals(trimResponse(jettyResponse),
				trimResponse(strResponse));
	}

	private String trimResponse(String str) {
		return str.replaceAll("\r", "").replaceAll("\n", "");
	}

	private String jettyResponse = "<html><body><h1>My Servlet GET response</h1></body></html>";
}
