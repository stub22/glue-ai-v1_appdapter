package org.appdapter.bundle.gui.osgi;

import ext.osgi.common.ExtBundleActivatorBase;
import org.osgi.framework.BundleContext;

public class Activator extends ExtBundleActivatorBase {

	@Override protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		super.handleFrameworkStartedEvent(bundleCtx);
		debugLoaders(org.appdapter.core.boot.ClassLoaderUtils.class);
		debugLoaders(org.appdapter.gui.demo.DemoBrowser.class);
		debugLoaders(org.appdapter.gui.browse.Utility.class);
		//   moved to o.a.bundle.fileconv Activator.
		// ext.bundle.openconverters.osgi.Activator.ensureConvertersClassesAreFindable();
	}
}
