package org.appdapter.bundle.gui.osgi;

import ext.osgi.common.ExtBundleActivatorBase;

public class Activator extends ExtBundleActivatorBase {

	@Override public void ensureExtClassesAreFindable() {
		if (isOSGIProperty("osgi-tests", true)) {
			debugLoaders(org.appdapter.core.boot.ClassLoaderUtils.class);
			debugLoaders(org.appdapter.gui.demo.DemoBrowser.class);
			debugLoaders(org.appdapter.gui.browse.Utility.class);
		}
	}
}
