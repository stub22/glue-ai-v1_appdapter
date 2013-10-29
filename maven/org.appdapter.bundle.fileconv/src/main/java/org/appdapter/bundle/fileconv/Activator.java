package org.appdapter.bundle.fileconv;

import ext.osgi.common.ExtBundleActivatorBase;
import org.osgi.framework.BundleContext;

public class Activator  extends ExtBundleActivatorBase {

	@Override protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		super.handleFrameworkStartedEvent(bundleCtx);
                if (isOSGIProperty("osgi-tests", true)) {
                     ext.bundle.openconverters.osgi.Activator.ensureConvertersClassesAreFindable();                   
                }
        }
}
