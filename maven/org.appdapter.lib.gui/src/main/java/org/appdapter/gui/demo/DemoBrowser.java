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

import org.appdapter.core.matdat.OfflineXlsSheetRepoSpec;
import org.appdapter.core.matdat.OnlineSheetRepoSpec;
import org.appdapter.core.matdat.RepoSpec;
import org.appdapter.demo.DemoBrowserUI;
import org.appdapter.demo.DemoBrowserCtrl;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoBrowser {

	public static void main(String[] args) {
		DemoBrowserCtrl browser = DemoBrowserUI.makeDemoNavigatorCtrl(args);
		browser.launchFrame(browser.getClass().getName());

		//DemoBrowser.addSampleElements(browser);

	}
	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args) {
		return (DemoNavigatorCtrl) DemoBrowserUI.makeDemoNavigatorCtrl(args);
	}
	
	private static void addSampleElements(DemoBrowserCtrl browser) {
		browser.attachChildUI("OnlineRepoSpec", makeOnlineRepoSpec(), false);
		browser.attachChildUI("OfflineRepoSpec", makeOfflineRepoSpec(), false);
	}

	public static RepoSpec makeOnlineRepoSpec() {
		if (true) {
			return new OnlineSheetRepoSpec(BMC_SHEET_KEY, BMC_NAMESPACE_SHEET_NUM, BMC_DIRECTORY_SHEET_NUM, null);
		}
		return null;
	}

	public static RepoSpec makeOfflineRepoSpec() {
		return new OfflineXlsSheetRepoSpec(BMC_WORKBOOK_PATH, BMC_NAMESPACE_SHEET_NAME, BMC_DIRECTORY_SHEET_NAME, null);
	}

	// These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
	//   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
	public static String BMC_SHEET_KEY = "0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c";
	public static int BMC_NAMESPACE_SHEET_NUM = 4;
	public static int BMC_DIRECTORY_SHEET_NUM = 3;

	// These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
	//   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
	// When exported to Disk
	public static String BMC_WORKBOOK_PATH = "GluePuma_BehavMasterDemo.xlsx";
	public static String BMC_NAMESPACE_SHEET_NAME = "Nspc";
	public static String BMC_DIRECTORY_SHEET_NAME = "Dir";

}
