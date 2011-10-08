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


package org.appdapter.osgi.felix.shell;

import java.net.URL;
import org.apache.log4j.PropertyConfigurator;
import org.appdapter.gui.demo.DemoBrowser;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.appdapter.gui.demo.DemoNavigatorCtrl;
import javax.swing.JFrame;

public class AppdapterFelixShellBundleActivator implements BundleActivator {
	static Logger theLogger = LoggerFactory.getLogger(AppdapterFelixShellBundleActivator.class);
	
	private JFrame	myDemoJFrame;
	
    public void start(BundleContext context) throws Exception {
		String startupMsg = getClass().getCanonicalName() + ".start(ctx=" + context + ")";
		System.out.println("[System.out]" + startupMsg);
		theLogger.info("[SLF4J]" + startupMsg);
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		ClassLoader localCL = getClass().getClassLoader();
		System.out.println("thread-context-CL=" + threadCL);
		System.out.println("local-CL=" + localCL);

		String resPath = "log4j.properties";
		
		URL threadURL = threadCL.getResource(resPath);
		URL localURL = localCL.getResource(resPath);
		System.out.println("threadCL resolved " + resPath + " to " + threadURL);
		System.out.println("localCL resolved " + resPath + " to " + localURL);
		
		// To get more determinism over when this happens (before other bundles that use logging are launched),
		// need to mess with Felix auto-start properties?
		System.out.println("[System.out] " + getClass().getCanonicalName() + " is forcing Log4J to read config from: " + localURL);
		PropertyConfigurator.configure(localURL);
		theLogger.info("[SLF4J]" + startupMsg);
		theLogger.info("Is SLF4J->Log4J logging working?");
		theLogger.info("Starting demo browser[");
		
		String args[] = null;
		DemoNavigatorCtrl dnc = DemoBrowser.makeDemoNavigatorCtrl(args);
		dnc.launchFrame("org.appdapter.osgi.felix.shell - DemoBrowser");
		myDemoJFrame = dnc.getFrame();
		// DemoBrowser.main(null);
		theLogger.info("]Finished starting browser, bundle activation .start() complete.");
    }

    public void stop(BundleContext context) throws Exception {
		String windupMsg = getClass().getCanonicalName() + ".stop(ctx=" + context + ")";
		theLogger.info("[SLF4J] " + windupMsg);		
		System.out.println("[System.out]" + windupMsg);
		theLogger.info("[SLF4J] disposing of demo window" );	
		myDemoJFrame.dispose();
		theLogger.info("[SLF4J] finished dispose()" );	
    }

}
