package org.appdapter.bundle.gui.osgi;

import org.appdapter.ext.osgi.common.ExtBundleActivatorBase;
import org.osgi.framework.BundleContext;

public class Activator extends ExtBundleActivatorBase {

	@Override protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		super.handleFrameworkStartedEvent(bundleCtx);
		debugLoaders(org.appdapter.core.boot.ClassLoaderUtils.class);
		debugLoaders(org.appdapter.gui.demo.DemoBrowser.class);
		debugLoaders(org.appdapter.gui.browse.Utility.class);
		org.appdapter.ext.bundle.openconverters.osgi.Activator.ensureConvertersClassesAreFindable();
	}
}
