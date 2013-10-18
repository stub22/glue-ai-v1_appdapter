/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.appdapter.osgi.core;

/**
 * @author Stu B. <www.texpedient.com>
 */
import org.appdapter.core.boot.ClassLoaderUtils;
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;

/**
 * Note:  Appdapter client bundles are *not* required to use this class, or to use
 * Log4J logging.  However, you can choose to extend this base class, in order to 
 * get access to some SLF4J-basd logging methods.
 * 
 * This BundleActivator extends BasicDebugger.  Thus, by extending it, your bundle gets
 * access to the logging methods of that class.  This class prints messages when its
 * start() and stop() methods are called, but otherwise takes no action. 
 * 
 * Optional
 * If your bundle is the "top" bundle of some application, then you can use the
 * forceLog4JConfig() method inherited from BasicDebugger to read config from a
 * log4j.properties file-resource at the root of your bundle.
 * 
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BundleActivatorBase extends BasicDebugger implements BundleActivator {

	final class GotFrameworkStartEvent implements FrameworkListener {
		public void frameworkEvent(FrameworkEvent fe) {
			int eventType = fe.getType();
			getLogger().info("************************ Got frameworkEvent with eventType=" + eventType + ", bundle=" + fe.getBundle());
			if (eventType == FrameworkEvent.STARTED) {
				getLogger().info("********  OSGi Framework has STARTED, calling dispatchFrameworkStartedEvent()");
				dispatchFrameworkStartedEvent(fe.getBundle(), fe.getThrowable());
			}
		}
	}

	public BundleActivatorBase() {
		ClassLoaderUtils.registerClassLoader(this, null);
	}

	/**
	 * There are two things you might do in your overriding start() method, which
	 * are generally only done in asingle  "top" bundle (generally corresponding to a project
	 * that you want to "run" from maven, or use as the seed of your own launch strategy).
	 * 
	 * If you call these methods from multiple bundles, then you will have to deal with
	 * the fact that those invocations (and their callbacks) may generally occur in any order.
	 * 
	 * 1) Call forceLog4JConfig() if you like Log4J logging.  
	 * 2) Call scheduleFrameworkStartEventHandler() to schedule a callback allowing you to
	 * safely begin your application setup *after* all initial bundles have been started.
	 * 
	 * @param bundleCtx
	 * @throws Exception 
	 */
	@Override public void start(BundleContext bundleCtx) throws Exception {
		ClassLoaderUtils.registerClassLoader(this, bundleCtx);
		getLogger().info(describe("start<BundleActivatorBase>", bundleCtx));
	}

	@Override public void stop(BundleContext bundleCtx) throws Exception {
		ClassLoaderUtils.unregisterClassLoader(this, bundleCtx);
		getLogger().info(describe("stop<BundleActivatorBase>", bundleCtx));
	}

	/** Override this method if you need notification after all bundles in your container have been started.
	 * Normally you would override this method in a "top" bundle to setup your application code.
	 * Important:  This handler will only be called if you called scheduleFrameworkStartEventHandler()
	 * from your bundle's start() method!
	 * @param bundleCtx - Fetched from the bundle of the FrameworkEvent, so it should be nice and fresh.
	 */
	protected void handleFrameworkStartedEvent(BundleContext bundleCtx) {
		getLogger().info("Default implementation of handleFrameworkStartedEvent() called on " + getClass() + ", you should override this!  BundleContext=" + bundleCtx);
	}

	/**
	 * Call this method from any bundle's start() to schedule a callback to its handleFrameworkStartedEvent() method.
	 * 
	 * @param bundleCtx - used to schedule the callback, and then forgotten.
	 */
	protected void scheduleFrameworkStartEventHandler(BundleContext bundleCtx) {
		bundleCtx.addFrameworkListener(new GotFrameworkStartEvent());
	}

	private void dispatchFrameworkStartedEvent(Bundle eventBundle, Throwable eventThrowable) {
		String thrownMsg = (eventThrowable == null) ? "OK" : eventThrowable.getClass().getName();
		getLogger().info("dispatchFrameworkStartedEvent<BundleActivatorBase> ( bundle={}, msg={}", eventBundle, thrownMsg);
		if (eventThrowable == null) {
			BundleContext bc = eventBundle.getBundleContext();
			if (bc == null) {
				getLogger().info("Cannot find bundle context for event bundle, so there will be no callback to app startup: {} ", eventBundle);
			}
			handleFrameworkStartedEvent(bc);
		} else {
			getLogger().warn("No callback to application startup, due to throwable ", eventThrowable);
		}
	}

	protected String describe(String action, BundleContext bundleCtx) {
		Bundle b = bundleCtx.getBundle();
		String msg = getClass().getCanonicalName() + "." + action + "(ctx=[" + bundleCtx + "], bundle=[" + b + "])";
		return msg;
	}

}
