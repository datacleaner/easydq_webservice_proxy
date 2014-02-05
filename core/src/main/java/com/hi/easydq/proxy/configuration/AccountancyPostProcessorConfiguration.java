package com.hi.easydq.proxy.configuration;

import com.hi.easydq.proxy.modules.api.prepostprocessing.Processor;
import com.hi.easydq.proxy.modules.prepostprocessing.Accountancy;
import com.hi.easydq.proxy.modules.prepostprocessing.AccountancyPostProcessor;

/**
 * {@link ProcessorConfiguration} for {@link AccountancyPostProcessor}.
 * 
 * @author Tomasz Guzialek
 * 
 */
public class AccountancyPostProcessorConfiguration extends
		ProcessorConfiguration {

	private Accountancy accountancy;

	@Override
	public Processor createProcessor() {
		return new AccountancyPostProcessor(accountancy);
	}

	public void setAccountancy(Accountancy accountancy) {
		this.accountancy = accountancy;
	}

}
