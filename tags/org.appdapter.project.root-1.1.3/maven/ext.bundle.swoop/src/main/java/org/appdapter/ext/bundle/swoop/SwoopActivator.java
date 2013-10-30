package org.appdapter.ext.bundle.swoop;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SwoopActivator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
		System.out.println("[System.out] " + this.getClass().getCanonicalName() + ".start(ctx=" + context + ")");
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("[System.out] " + this.getClass().getCanonicalName() + ".stop(ctx=" + context + ")");
    }

}
