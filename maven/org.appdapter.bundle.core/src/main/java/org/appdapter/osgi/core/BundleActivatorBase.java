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

import java.net.URL;
import org.appdapter.bind.log4j.Log4jFuncs;




public abstract class BundleActivatorBase implements BundleActivator  {
	protected abstract Logger getLogger();
			
    @Override public void start(BundleContext bundleCtx) throws Exception {
		String startupMsg = getClass().getCanonicalName() + ".start(ctx=" + bundleCtx + ")";
		System.out.println("[System.out]" + startupMsg);
		Logger log = getLogger();
		log.info("[SLF4J]" + startupMsg);
		Bundle b = bundleCtx.getBundle();
		log.info("bundle=" + b);
    }

    @Override
	public void stop(BundleContext context) throws Exception {
		String windupMsg = getClass().getCanonicalName() + ".stop(ctx=" + context + ")";
		System.out.println("[System.out]" + windupMsg);		
		Logger log = getLogger();
		log.info("[SLF4J]" + windupMsg);		
	}
	
	protected void forceLog4jConfig() {
		Logger logger = getLogger();

		// To get more determinism over when this happens (before other bundles that use logging are launched),
		// need to mess with Felix auto-start properties?
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		ClassLoader localCL = getClass().getClassLoader();
		System.out.println("thread-context-CL=" + threadCL);
		System.out.println("local-CL=" + localCL);

		String resPath = "log4j.properties";
		
		URL threadURL = threadCL.getResource(resPath);
		URL localURL = localCL.getResource(resPath);
		System.out.println("[System.out] threadCL resolved " + resPath + " to threadURL " + threadURL);
		System.out.println("[System.out] localCL resolved  " + resPath + " to  localURL" + localURL);
		System.out.println("[System.out] " + getClass().getCanonicalName() + " is forcing Log4J to read config from localURL: " + localURL);
		Log4jFuncs.forceLog4jConfig(localURL);
		getLogger().info("{forceLog4JConfig} - This here message should be conveyed by SLF4J->Log4J");
		
	}

}
