package org.appdapter.osgi.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppdapterCoreBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(AppdapterCoreBundleActivator.class);

	@Override
	public Logger getLogger() {
		return theLogger;
	}

}
