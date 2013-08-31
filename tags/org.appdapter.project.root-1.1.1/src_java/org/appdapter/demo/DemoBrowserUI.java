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
package org.appdapter.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoBrowserUI {

	static Logger theLogger = LoggerFactory.getLogger(DemoBrowserUI.class);
	public static DemoNavigatorCtrlFactory demoBrowserFactory = null;

	public static void registerDemo(DemoNavigatorCtrlFactory crtlMaker) {
		demoBrowserFactory = crtlMaker;
	}

	public static DemoBrowserCtrl makeDemoNavigatorCtrl(String[] args) {
		if (demoBrowserFactory == null)
			return null;
		return demoBrowserFactory.makeDemoNavigatorCtrl(args, false);
	}

	public static DemoBrowserCtrl makeDemoNavigatorCtrl(String[] args, boolean addExamples) {
		if (demoBrowserFactory == null)
			return null;
		return demoBrowserFactory.makeDemoNavigatorCtrl(args, addExamples);
	}

	public static void testLoggingSetup() {
		try {
			Object theLogger2 = LoggerFactory.getLogger(DemoBrowserUI.class);
			assert (theLogger == theLogger2);
		} catch (Throwable anything) {
			// if the logger isnt working we may as well just print this error and not log it :)
			anything.printStackTrace();

		}
	}
}
