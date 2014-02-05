package com.hi.easydq.proxy.configuration;

import org.apache.commons.dbcp.BasicDataSource;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.jdbc.JdbcDataContext;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for a {@link UpdateableDataContext}.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class DataContextConfiguration implements
		FactoryBean<UpdateableDataContext> {

	private String driverClass;
	private String url;
	private String username;
	private String password;

	@Override
	public UpdateableDataContext getObject() throws Exception {
		final BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl(url);
		dataSource.setDriverClassName(driverClass);
		dataSource.setUsername(username);
		dataSource.setPassword(password);

		return new JdbcDataContext(dataSource);
	}

	@Override
	public Class<?> getObjectType() {
		return UpdateableDataContext.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
