package org.appdapter.ext.bundle.math.jscience;

import org.osgi.framework.BundleContext;

// we extend ExtBundleActivatorBase so we get a free call to handleFrameworkStartedEvent
public class Activator extends ext.osgi.common.ExtBundleActivatorBase {

	public void start(BundleContext context) throws Exception {
		// TODO add activation code here
	}

	public void stop(BundleContext context) throws Exception {
		// TODO add deactivation code here
	}

	@Override protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		debugLoaders(org.jscience.mathematics.vector.SparseVector.class);
	}
}
