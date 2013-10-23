package org.appdapter.ext.osgi.common;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ExtOSGiCommonBundleActivator extends ExtBundleActivatorBase implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		trace(this.getClass().getCanonicalName() + ".start(ctx=" + context + ")");
	}

	public void stop(BundleContext context) throws Exception {
		trace(this.getClass().getCanonicalName() + ".stop(ctx=" + context + ")");
	}
}
