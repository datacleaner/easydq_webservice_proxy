package com.hi.easydq.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hi.easydq.proxy.configuration.ForwardingModuleConfiguration;
import com.hi.easydq.proxy.configuration.ModuleConfiguration;
import com.hi.easydq.proxy.configuration.WebProxyConfigurationImpl;
import com.hi.easydq.proxy.modules.prepostprocessing.NoOperationProcessor;

public class WebServiceProxyDatacleanerIntegrationTest {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory
			.getLogger(WebServiceProxyDatacleanerIntegrationTest.class);

	private static WebServiceProxy webServiceProxy;

	@After
	public void tearDown() throws Exception {
		webServiceProxy.stop();
	}

	@Test
	public void testDatacleanerRobotsForwarding() throws Exception {
		List<ModuleConfiguration> modules = new ArrayList<ModuleConfiguration>();
		ForwardingModuleConfiguration forwardingModuleConfiguration = new ForwardingModuleConfiguration();
		forwardingModuleConfiguration.setPath("/resources/robots.txt");
		forwardingModuleConfiguration.setHostname("datacleaner.org");
		forwardingModuleConfiguration.setPort(80);
		forwardingModuleConfiguration
				.setPreProcessor(new NoOperationProcessor());
		forwardingModuleConfiguration
				.setPostProcessor(new NoOperationProcessor());
		modules.add(forwardingModuleConfiguration);

		WebProxyConfigurationImpl webProxyConfiguration = new WebProxyConfigurationImpl();
		webProxyConfiguration.setPort(8181);

		webServiceProxy = new WebServiceProxy(webProxyConfiguration, modules);
		webServiceProxy.run();

		HttpHost targetHost = new HttpHost("localhost",
				webProxyConfiguration.getPort(), "http");

		CloseableHttpClient httpClient = HttpClients.custom().build();

		HttpGet httpGet = new HttpGet("/resources/robots.txt");

		CloseableHttpResponse resp = httpClient.execute(targetHost, httpGet);
		String strResponse;
		try {
			HttpEntity entity = resp.getEntity();
			strResponse = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} finally {
			resp.close();
		}
		httpClient.close();

		Assert.assertEquals(trimResponse(robotsResponse),
				trimResponse(strResponse));
	}

	@Test
	public void testDatacleanerRobotsDiscarding() throws Exception {
		List<ModuleConfiguration> modules = new ArrayList<ModuleConfiguration>();
		ForwardingModuleConfiguration forwardingModuleConfiguration = new ForwardingModuleConfiguration();
		forwardingModuleConfiguration.setPath("/someInvalidModule");
		forwardingModuleConfiguration.setHostname("datacleaner.org");
		forwardingModuleConfiguration.setPort(80);
		forwardingModuleConfiguration
				.setPreProcessor(new NoOperationProcessor());
		forwardingModuleConfiguration
				.setPostProcessor(new NoOperationProcessor());
		modules.add(forwardingModuleConfiguration);

		WebProxyConfigurationImpl webProxyConfiguration = new WebProxyConfigurationImpl();
		webProxyConfiguration.setPort(8181);

		webServiceProxy = new WebServiceProxy(webProxyConfiguration, modules);
		webServiceProxy.run();

		HttpHost targetHost = new HttpHost("localhost",
				webProxyConfiguration.getPort(), "http");

		CloseableHttpClient httpClient = HttpClients.custom().build();

		HttpGet httpGet = new HttpGet("/resources/robots.txt");

		CloseableHttpResponse resp = httpClient.execute(targetHost, httpGet);
		String strResponse;
		try {
			HttpEntity entity = resp.getEntity();
			strResponse = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} finally {
			resp.close();
		}
		httpClient.close();

		Assert.assertEquals(trimResponse(""), trimResponse(strResponse));
	}

	@Test
	public void testDatacleanerDocsFullForwarding() throws Exception {
		List<ModuleConfiguration> modules = new ArrayList<ModuleConfiguration>();
		ForwardingModuleConfiguration forwardingModuleConfiguration = new ForwardingModuleConfiguration();
		forwardingModuleConfiguration.setPath("/docs");
		forwardingModuleConfiguration.setHostname("datacleaner.org");
		forwardingModuleConfiguration.setPort(80);
		forwardingModuleConfiguration
				.setPreProcessor(new NoOperationProcessor());
		forwardingModuleConfiguration
				.setPostProcessor(new NoOperationProcessor());
		modules.add(forwardingModuleConfiguration);

		WebProxyConfigurationImpl webProxyConfiguration = new WebProxyConfigurationImpl();
		webProxyConfiguration.setPort(8181);

		webServiceProxy = new WebServiceProxy(webProxyConfiguration, modules);
		webServiceProxy.run();

		HttpHost targetHost = new HttpHost("localhost",
				webProxyConfiguration.getPort(), "http");

		CloseableHttpClient httpClient = HttpClients.custom().build();

		HttpGet httpGet = new HttpGet("/docs");

		CloseableHttpResponse resp = httpClient.execute(targetHost, httpGet);

		String strResponse;
		try {
			HttpEntity entity = resp.getEntity();
			strResponse = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} finally {
			resp.close();
		}

		httpClient.close();

		Assert.assertTrue(strResponse.contains("<h1>Reference documentation</h1>"));
	}

	@Test
	public void testDatacleanerDocsSemiDiscarding() throws Exception {
		List<ModuleConfiguration> modules = new ArrayList<ModuleConfiguration>();
		ForwardingModuleConfiguration forwardingModuleConfiguration = new ForwardingModuleConfiguration();
		forwardingModuleConfiguration.setPath("/docs");
		forwardingModuleConfiguration.setHostname("datacleaner.org");
		forwardingModuleConfiguration.setPort(80);
		forwardingModuleConfiguration
				.setPreProcessor(new NoOperationProcessor());
		forwardingModuleConfiguration
				.setPostProcessor(new NoOperationProcessor());
		modules.add(forwardingModuleConfiguration);

		WebProxyConfigurationImpl webProxyConfiguration = new WebProxyConfigurationImpl();
		webProxyConfiguration.setPort(8181);

		webServiceProxy = new WebServiceProxy(webProxyConfiguration, modules);
		webServiceProxy.run();

		HttpHost targetHost = new HttpHost("localhost",
				webProxyConfiguration.getPort(), "http");

		CloseableHttpClient httpClient = HttpClients.custom().build();

		HttpGet httpGet = new HttpGet("/docs");

		CloseableHttpResponse resp = httpClient.execute(targetHost, httpGet);
		String strResponse;
		try {
			HttpEntity entity = resp.getEntity();
			strResponse = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} finally {
			resp.close();
		}
		httpClient.close();

		Assert.assertTrue(strResponse.contains("<h1>Reference documentation</h1>"));
	}

	@Test
	public void testDatacleanerDocsFullForwardingConcurrent() throws Throwable {
		List<ModuleConfiguration> modules = new ArrayList<ModuleConfiguration>();
		ForwardingModuleConfiguration forwardingModuleConfiguration = new ForwardingModuleConfiguration();
		forwardingModuleConfiguration.setPath("/docs");
		forwardingModuleConfiguration.setHostname("datacleaner.org");
		forwardingModuleConfiguration.setPort(80);
		forwardingModuleConfiguration
				.setPreProcessor(new NoOperationProcessor());
		forwardingModuleConfiguration
				.setPostProcessor(new NoOperationProcessor());
		modules.add(forwardingModuleConfiguration);

		final WebProxyConfigurationImpl webProxyConfiguration = new WebProxyConfigurationImpl();
		webProxyConfiguration.setPort(8181);

		webServiceProxy = new WebServiceProxy(webProxyConfiguration, modules);
		webServiceProxy.run();

		final int THREAD_COUNT = 2;
		final int REQUESTS_PER_THREAD = 3;

		final Thread[] threads = new Thread[THREAD_COUNT];

		final AtomicBoolean success = new AtomicBoolean(true);
		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();

		for (int i = 0; i < THREAD_COUNT; i++) {
			threads[i] = new Thread() {
				public void run() {
					try {
						for (int i = 0; i < REQUESTS_PER_THREAD; i++) {
							HttpHost targetHost = new HttpHost("localhost",
									webProxyConfiguration.getPort(), "http");

							CloseableHttpClient httpClient = HttpClients
									.custom().build();

							HttpGet httpGet = new HttpGet("/docs");

							CloseableHttpResponse resp = httpClient.execute(
									targetHost, httpGet);
							String strResponse = EntityUtils.toString(resp
									.getEntity());
							Assert.assertTrue(strResponse.contains("<h1>Reference documentation</h1>"));

							resp.close();
							httpClient.close();
						}
					} catch (Throwable e) {
						success.set(false);
						exception.set(e);
						;
					}
				};
			};
			threads[i].start();
		}

		for (int i = 0; i < THREAD_COUNT; i++) {
			threads[i].join();
			if (!success.get()) {
				throw exception.get();
			}
		}

		if (!success.get()) {
			throw exception.get();
		}
	}

	private String trimResponse(String str) {
		return str.replaceAll("\r", "").replaceAll("\n", "");
	}

	private String robotsResponse = "User-agent: *Sitemap: http://datacleaner.eobjects.org/sitemap.xmlDisallow: /resources/Disallow: /ws/Disallow: /3.0/";

}
