package org.appdapter.ext.osgi.felix.shell;

import org.osgi.framework.BundleContext;

import ext.osgi.common.ExtBundleActivatorBase;

public class ExtOSGiFelixShellBundleActivator extends ExtBundleActivatorBase {

    public void stop(BundleContext context) throws Exception {
        System.out.println("[System.out] " + this.getClass().getCanonicalName() + ".stop(ctx=" + context + ")");
    }

}
