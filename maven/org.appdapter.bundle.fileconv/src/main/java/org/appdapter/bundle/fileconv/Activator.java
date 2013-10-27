package org.appdapter.bundle.fileconv;

import ext.osgi.common.ExtBundleActivatorBase;
import org.osgi.framework.BundleContext;

public class Activator  extends ExtBundleActivatorBase {

	@Override protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		super.handleFrameworkStartedEvent(bundleCtx);
		ext.bundle.openconverters.osgi.Activator.ensureConvertersClassesAreFindable();
	}
}
