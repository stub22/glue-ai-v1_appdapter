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

package org.appdapter.gui.demo;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoBrowser extends DemoBrowser_NewGUI {

	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args) {
		return DemoBrowser_NewGUI.makeDemoNavigatorCtrl(args, false);
	}

	public static void testLoggingSetup() {
		try {
			getLogger();
		} catch (Throwable anything) {
			// if the logger isnt working we may as well just print this error and not log it :)
			anything.printStackTrace();
			
		}
	}
}
