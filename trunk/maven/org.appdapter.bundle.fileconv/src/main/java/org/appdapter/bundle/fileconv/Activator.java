package org.appdapter.bundle.fileconv;

import org.appdapter.fileconv.FileStreamUtils;

import ext.osgi.common.ExtBundleActivatorBase;

public class Activator extends ExtBundleActivatorBase {

	@Override public void ensureExtClassesAreFindable() {
		if (isOSGIProperty("osgi-tests", true)) {
			FileStreamUtils.canLoadWorkbooks();
		}
	}
}
