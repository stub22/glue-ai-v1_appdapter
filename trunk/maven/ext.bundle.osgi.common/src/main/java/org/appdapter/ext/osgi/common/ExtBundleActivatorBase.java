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
package org.appdapter.ext.osgi.common;

/**
 * @author Stu B. <www.texpedient.com>
 */
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public abstract class ExtBundleActivatorBase implements BundleActivator {

	boolean eventScenduled = false;
	boolean dispatchedFrameworkStartedEvent = false;

	final class GotFrameworkStartEvent implements FrameworkListener {
		public void frameworkEvent(FrameworkEvent fe) {
			int eventType = fe.getType();
			ExtOSGiCommonBundleActivator.trace("************************ Got frameworkEvent with eventType=" + eventType + ", bundle=" + fe.getBundle());
			if (eventType == FrameworkEvent.STARTED) {
				if (dispatchedFrameworkStartedEvent)
					return;
				dispatchedFrameworkStartedEvent = true;
				ExtOSGiCommonBundleActivator.warning("********  OSGi Framework has STARTED, calling dispatchFrameworkStartedEvent()");
				try {
					dispatchFrameworkStartedEvent(fe.getBundle(), fe.getThrowable());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
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
		ExtOSGiCommonBundleActivator.warning(describe("start<BundleActivatorBase>", bundleCtx));
		scheduleFrameworkStartEventHandler(bundleCtx);
	}

	@Override public void stop(BundleContext bundleCtx) throws Exception {
		ExtOSGiCommonBundleActivator.warning(describe("stop<BundleActivatorBase>", bundleCtx));
	}

	/** Override this method if you need notification after all bundles in your container have been started.
	 * Normally you would override this method in a "top" bundle to setup your application code.
	 * Important:  This handler will only be called if you called scheduleFrameworkStartEventHandler()
	 * from your bundle's start() method!
	 * @param bundleCtx - Fetched from the bundle of the FrameworkEvent, so it should be nice and fresh.
	 * @throws Exception
	 */
	protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		ExtOSGiCommonBundleActivator.warning("Default implementation of handleFrameworkStartedEvent() called on " + getClass() + ", you should override this!  BundleContext=" + bundleCtx);
	}

	/**
	 * Call this method from any bundle's start() to schedule a callback to its handleFrameworkStartedEvent() method.
	 *
	 * @param bundleCtx - used to schedule the callback, and then forgotten.
	 */
	protected void scheduleFrameworkStartEventHandler(BundleContext bundleCtx) {
		if (!eventScenduled) {
			eventScenduled = true;
			bundleCtx.addFrameworkListener(new GotFrameworkStartEvent());
		}
	}

	private void dispatchFrameworkStartedEvent(Bundle eventBundle, Throwable eventThrowable) throws Exception {
		String thrownMsg = (eventThrowable == null) ? "OK" : eventThrowable.getClass().getName();
		ExtOSGiCommonBundleActivator.warning("dispatchFrameworkStartedEvent<BundleActivatorBase> ( bundle={}, msg={}", eventBundle, thrownMsg);
		if (eventThrowable == null) {
			BundleContext bc = eventBundle.getBundleContext();
			if (bc == null) {
				ExtOSGiCommonBundleActivator.warning("Cannot find bundle context for event bundle, so there will be no callback to app startup: {} ", eventBundle);
			}
			handleFrameworkStartedEvent(bc);
		} else {
			ExtOSGiCommonBundleActivator.warning("No callback to application startup, due to throwable ", eventThrowable);
		}
	}

	protected String describe(String action, BundleContext bundleCtx) {
		Bundle b = bundleCtx.getBundle();
		String msg = getClass().getCanonicalName() + "." + action + "(ctx=[" + bundleCtx + "], bundle=[" + b + "])";
		return msg;
	}

	public static void trace(String string, Object... args) {
		System.out.println("[System.out] Trace: " + string);
	}

	public static void warning(String string, Object... args) {
		System.err.println("[System.err] Warning: " + string);
	}

	public static void debugLoaders(Class clazz) {
		clazz.getDeclaredMethods();
		clazz.getDeclaredFields();
		Class class2;
		try {
			class2 = Class.forName(clazz.getName(), true, null);
			if (class2 != clazz) {
				warning("Classes not same as in current loader " + clazz);
				debugLoadersInfo(clazz);
				debugLoadersInfo(class2);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			warning("Class missing in current loader " + clazz);
			debugLoadersInfo(clazz);
			if (e instanceof Error)
				throw (Error) e;
		}
	}

	public static void debugLoadersInfo(Class clazz) {
		ClassLoader myLoader = clazz.getClassLoader();
		ClassLoader pLoader = myLoader.getParent();
		ClassLoader gpLoader = pLoader.getParent();
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		ClassLoader systemCL = ClassLoader.getSystemClassLoader();

		trace("Classloader for the main class is: " + myLoader);
		trace("Parent Classloader is: " + pLoader);
		trace("GrandParent Classloader is: " + gpLoader);
		trace("ContextClassloader for currentThread is: " + threadCL);
		trace("SystemCL is: " + systemCL);
	}

	public static void debugResolve(String desc, ClassLoader cl, String resourcePath) {
		trace("------------");
		URL rurl = cl.getResource(resourcePath);
		trace("Classloader[" + desc + ", " + cl + "].getResource(" + resourcePath + ") = " + rurl);
		URL surl = cl.getSystemResource(resourcePath);
		trace("Classloader[" + desc + ", " + cl + "].getSystemResource(" + resourcePath + ") = " + surl);
		trace("------------");
	}

}
