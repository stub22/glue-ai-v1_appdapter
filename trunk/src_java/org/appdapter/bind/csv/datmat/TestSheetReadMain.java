/*
 *  Copyright 2012 by The Friendularity Project (www.friendularity.org).
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
package org.appdapter.bind.csv.datmat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.appdapter.core.log.BasicDebugger;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TestSheetReadMain {

	static BasicDebugger theDbg = new BasicDebugger();
	static String gdocPubUrlWithKey = 
			"https://docs.google.com/spreadsheet/pub?key=0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc";
	static String tmpExtender = "&single=true&gid=7&range=A2%3AK999&output=csv";

	public static void main(String args[]) {
		String fullUrlTxt = gdocPubUrlWithKey + tmpExtender;
		Reader shdr = makeSheetDataReader(fullUrlTxt);
		theDbg.logInfo("Got sheet reader: " + shdr);
		List<String[]> resultRows = readAllRows(shdr);
		// theDbg.logInfo("Got result rows: " + resultRows);
		for (String[] cells : resultRows) {
			theDbg.logInfo("--------------------------------Row Break------------------------");
			for (String c : cells) {
				theDbg.logInfo("Got cell: " + c);
			}
		}
	}


	public static Reader makeSheetDataReader(String fullUrlTxt) {
		Reader sheetReader = null;
		
		try {
			URL url = new URL(fullUrlTxt);
			URLConnection urlc = url.openConnection();

			InputStream istream = urlc.getInputStream();
			sheetReader = new InputStreamReader(istream);

		} catch (Throwable t) {
			theDbg.logError("Cannot read[" + fullUrlTxt + "]", t);

		}
		return sheetReader;
	}
	
	static List<String[]> theFailedRowList = new ArrayList<String[]>();
	public static List<String[]> readAllRows(Reader matDataReader) {
		List<String[]> resultRows = theFailedRowList;
		try {
			CSVReader csvr = new CSVReader(matDataReader);
			resultRows = csvr.readAll();
		} catch (Throwable t) {
			theDbg.logError("Failed during CSV parse", t);
		}
		return resultRows;
	}
}
