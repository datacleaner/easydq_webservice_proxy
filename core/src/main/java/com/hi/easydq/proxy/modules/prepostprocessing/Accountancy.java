package com.hi.easydq.proxy.modules.prepostprocessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class storing billing information for the web services.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class Accountancy {

	private static final Logger logger = LoggerFactory
			.getLogger(Accountancy.class);

	/**
	 * The logger where all the accountancy items should be written in case of
	 * stable storage write error.
	 */
	private static final Logger recoveryLogger = LoggerFactory
			.getLogger("AccountancyRecoveryLogger");

	/**
	 * The future object for the flushing task.
	 */
	private ScheduledFuture<?> flusherFuture;

	/**
	 * The list of accountancy items to be flushed.
	 */
	private final BlockingQueue<AccountancyItem> accountancyItems = new LinkedBlockingQueue<AccountancyItem>();

	/**
	 * Apache MetaModel handle to the datastore.
	 */
	private UpdateableDataContext dataContext;

	/**
	 * Starts a new thread flushing the accountancy items with specified
	 * frequency.
	 * 
	 * @param dataContext
	 *            The handle to the datastore.
	 * @param amount
	 *            The amount of units.
	 * @param units
	 *            Time unit.
	 */
	public Accountancy(UpdateableDataContext dataContext, int amount,
			TimeUnit units) {
		runFlushingThread(dataContext, amount, units);
	}

	/**
	 * Starts a new thread flushing the accountancy items with default frequency
	 * of 10 seconds.
	 * 
	 * @param dataContext
	 *            The handle to the datastore.
	 */
	public Accountancy(UpdateableDataContext dataContext) {
		runFlushingThread(dataContext, 10, TimeUnit.SECONDS);
	}

	/**
	 * The private method invoked from the constructor starting the flushing
	 * thread with specified frequency.
	 * 
	 * @param dataContext
	 *            The handle to the datastore.
	 * @param amount
	 *            The amount of units.
	 * @param units
	 *            Time unit.
	 */
	private void runFlushingThread(final UpdateableDataContext dataContext,
			int amount, TimeUnit units) {
		// Runs the flushAccountancyItems method every 10 seconds.
		this.dataContext = dataContext;
		final ScheduledExecutorService executorService = Executors
				.newSingleThreadScheduledExecutor();
		flusherFuture = executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				flushItems();
			}

		}, 0, amount, units);
	}

	/**
	 * Makes a copy of the accountancy items and flushes the copy to the stable
	 * storage. If failed, the accountancy items are writte to the recovery log.
	 */
	protected void flushItems() {
		if (accountancyItems.isEmpty()) {
			return;
		}
		final List<AccountancyItem> copy = new ArrayList<AccountancyItem>();
		try {
			accountancyItems.drainTo(copy);

			dataContext.executeUpdate(new UpdateScript() {

				@Override
				public void run(UpdateCallback callback) {
					logger.info("Flushing " + copy.size()
							+ ") accountancy items to storage.");
					for (AccountancyItem accountancyItem : copy) {
						logger.debug(accountancyItem.toString());
						callback.insertInto("accountancy_items")
								.value("username",
										accountancyItem.getCustomer()
												.getUsername())
								.value("service_name",
										accountancyItem.getServiceName())
								.value("timestamp",
										accountancyItem.getTimeStamp())
								.execute();
					}
					logger.info("Finished flushing " + copy.size()
							+ ") accountancy items to storage.");
				}
			});
		} catch (Exception e) {
			logger.error(
					"Flushing error occured. Logging accountancy items to recovery log.",
					e);
			for (AccountancyItem accountancyItem : copy)
				recoveryLogger.error(accountancyItem.toString());
		}
	}

	/**
	 * Adds the entry to the storage map.
	 * 
	 * @param customer
	 * @param accountancyItem
	 */
	public void addItem(AccountancyItem accountancyItem) {
		accountancyItems.add(accountancyItem);
	}

	/**
	 * Return the unmodifiable collection for inspecting purposes.
	 * 
	 * @return The unmodifiable collection of AccountancyItems.
	 */
	public Collection<AccountancyItem> getAccountancyItems() {
		return Collections.unmodifiableCollection(accountancyItems);
	}

	/**
	 * Stops the background worker from flushing the accountancy items to the
	 * persistent storage.
	 */
	public void close() {
		try {
			flusherFuture.cancel(false);
		} catch (Exception e) {
			// don't let this break anything
			logger.debug("Unexpected exception while closing Accountancy", e);
		}
	}

}
