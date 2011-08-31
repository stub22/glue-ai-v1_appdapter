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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger; 
public abstract class BundleActivatorBase implements BundleActivator  {
	protected abstract Logger getLogger();
	
    @Override
	public void start(BundleContext context) throws Exception {
		String startupMsg = getClass().getCanonicalName() + ".start(ctx=" + context + ")";
		System.out.println("[System.out]" + startupMsg);
		Logger log = getLogger();
		log.info("[SLF4J]" + startupMsg);
    }

    @Override
	public void stop(BundleContext context) throws Exception {
		String windupMsg = getClass().getCanonicalName() + ".stop(ctx=" + context + ")";
		System.out.println("[System.out]" + windupMsg);		
		Logger log = getLogger();
		log.info("[SLF4J]" + windupMsg);		
	}
}
