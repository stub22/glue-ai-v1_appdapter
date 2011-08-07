package org.appdapter.osgi.shelldemo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
		System.out.println("Shelldemo bundle starting");
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("Shelldemo bundle stopping");
    }

}
