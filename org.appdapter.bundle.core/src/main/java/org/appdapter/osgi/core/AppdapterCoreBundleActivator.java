package org.appdapter.osgi.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppdapterCoreBundleActivator implements BundleActivator {
	static Logger theLogger = LoggerFactory.getLogger(AppdapterCoreBundleActivator.class);
    public void start(BundleContext context) throws Exception {
		theLogger.info(getClass().getCanonicalName() + ".start(ctx=" + context + ")BEGIN-[");
        // org.appdapter.gui.demo.DemoBrowser.main(null);
		theLogger.info(getClass().getCanonicalName() + ".start(ctx=" + context + ")]-END");
    }

    public void stop(BundleContext context) throws Exception {
		theLogger.info(getClass().getCanonicalName() + ".stop(ctx=" + context + ")BEGIN-[");
          // TODO add deactivation code here
		theLogger.info(getClass().getCanonicalName() + ".stop(ctx=" + context + ")]-END");
    }

}
