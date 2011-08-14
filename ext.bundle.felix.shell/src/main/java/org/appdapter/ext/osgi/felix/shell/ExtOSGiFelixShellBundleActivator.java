package org.appdapter.ext.osgi.felix.shell;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ExtOSGiFelixShellBundleActivator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        System.out.println("[System.out] " + this.getClass().getCanonicalName() + ".start(ctx=" + context + ")");
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("[System.out] " + this.getClass().getCanonicalName() + ".stop(ctx=" + context + ")");
    }

}
