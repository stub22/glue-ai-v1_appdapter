package org.appdapter.osgi.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppdapterBundleActivator implements BundleActivator {
	static Logger theLogger = LoggerFactory.getLogger(AppdapterBundleActivator.class);

    public void start(BundleContext context) throws Exception {
		theLogger.info("AppdapterBundleActivator start() - BEGIN");
        // org.appdapter.gui.main.TestBrowse.main(null);
		theLogger.info("AppdapterBundleActivator start() - END");
    }

    public void stop(BundleContext context) throws Exception {
		theLogger.info("AppdapterBundleActivator stop() - BEGIN");
        // TODO add deactivation code here
		theLogger.info("AppdapterBundleActivator stop() - END");
    }

}
