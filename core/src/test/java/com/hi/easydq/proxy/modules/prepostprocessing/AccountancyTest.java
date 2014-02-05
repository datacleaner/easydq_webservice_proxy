package com.hi.easydq.proxy.modules.prepostprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.pojo.PojoDataContext;
import org.junit.Test;

import com.hi.easydq.proxy.users.Customer;

public class AccountancyTest {

	@Test
	public void flushTestWithMockDataContext() throws InterruptedException {
		PojoDataContext dataContext = new PojoDataContext("webserviceproxy");
		dataContext.executeUpdate(new UpdateScript() {
			@Override
			public void run(UpdateCallback callback) {
				callback.createTable("webserviceproxy", "accountancy_items")
						.withColumn("username").withColumn("service_name")
						.withColumn("timestamp").execute();
			}
		});
		
		Accountancy accountancy = new Accountancy(dataContext);
		accountancy.addItem(new AccountancyItem(new Customer("mockUserName", "mockPassword"), "mockServiceName"));
		accountancy.addItem(new AccountancyItem(new Customer("mockUserName", "mockPassword"), "mockServiceName"));
		accountancy.addItem(new AccountancyItem(new Customer("mockUserName", "mockPassword"), "mockServiceName"));
		
		accountancy.flushItems();
		
		DataSet dataSet = dataContext.query().from("accountancy_items").selectCount().execute();
		assertTrue(dataSet.next());
		
		assertEquals("Row[values=[3]]", dataSet.getRow().toString());
		
		assertFalse(dataSet.next());
	}

	@Test
	public void flushTestWithMocks() throws InterruptedException {
		UpdateableDataContext dataContext = EasyMock
				.createMock(UpdateableDataContext.class);

		Capture<UpdateScript> updateScriptCapture = new Capture<UpdateScript>();
		dataContext.executeUpdate(EasyMock.capture(updateScriptCapture));

		EasyMock.replay(dataContext);

		Accountancy accountancy = new Accountancy(dataContext);
		accountancy.addItem(new AccountancyItem(null, null));
		accountancy.addItem(new AccountancyItem(null, null));
		accountancy.addItem(new AccountancyItem(null, null));

		accountancy.flushItems();

		EasyMock.verify(dataContext);
	}

}
