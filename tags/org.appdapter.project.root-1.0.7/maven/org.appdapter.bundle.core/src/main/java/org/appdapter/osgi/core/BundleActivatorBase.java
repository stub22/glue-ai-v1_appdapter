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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger; 

import org.appdapter.core.log.BasicDebugger;

import java.net.URL;
import org.appdapter.bind.log4j.Log4jFuncs;




public abstract class BundleActivatorBase extends BasicDebugger implements BundleActivator  {
	// protected abstract Logger getLogger();
		
	protected String describe(String action, BundleContext bundleCtx) { 
		Bundle b = bundleCtx.getBundle();
		String msg = getClass().getCanonicalName() + "." + action + "(ctx=[" + bundleCtx + "], bundle=[" + b + "])";
		return msg;
	}
    @Override public void start(BundleContext bundleCtx) throws Exception {
		logInfo(describe("start<BundleActivatorBase>", bundleCtx));
    }

    @Override public void stop(BundleContext bundleCtx) throws Exception {
		logInfo(describe("stop<BundleActivatorBase>", bundleCtx));	
	}
	
}
