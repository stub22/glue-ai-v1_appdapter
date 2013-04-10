/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.*;

import com.hp.hpl.jena.sparql.pfunction.library.container;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FileStreamUtils {

	public static String getFileExt(String srcPath) {
		int at = srcPath.lastIndexOf('.');
		if (at < 0)
			return null;
		return srcPath.substring(at + 1).toLowerCase();
	}

	public static Reader sheetToReader(Sheet sheet) {
		StringBuffer sheetBuff = new StringBuffer();
		if (sheet.getPhysicalNumberOfRows() == 0) {
			throw new RuntimeException("No rows on sheet: " + sheet);
		}
		int width = getSheetWidth(sheet);
		int maxInclusive = sheet.getLastRowNum();
		for (int i = sheet.getFirstRowNum(); i <= maxInclusive; i++) {
			StringBuffer strBuff = new StringBuffer();
			Row row = sheet.getRow(i);

			int rwInclusve = row.getLastCellNum();
			for (int j = 0; j <= rwInclusve; j++) {
				Cell cell = row.getCell(j);
				if (j > 0)
					strBuff.append(",");
				if (cell == null)
					continue;
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					strBuff.append(cell.getNumericCellValue());
					continue;
				case Cell.CELL_TYPE_BLANK:
					continue;
				case Cell.CELL_TYPE_STRING:
					strBuff.append(escapeCSV(cell.getStringCellValue()));
					continue;
				case Cell.CELL_TYPE_FORMULA:
					strBuff.append(escapeCSV(cell.getCellFormula()));
					continue;
				default:
					break;
				}
				String str = cell.getStringCellValue();
				if (str == null)
					continue;
				strBuff.append(str);
			}

			// pad the rest
			int pad = width - rwInclusve;
			for (int j = 0; j < pad; j++) {
				strBuff.append(",");
			}
			sheetBuff.append(strBuff.toString().trim() + "\n");
		}
		return new StringReader(sheetBuff.toString().trim());
	}

	private static int getSheetWidth(Sheet sheet) {
		return sheet.getRow(sheet.getFirstRowNum()).getLastCellNum() + 1;
	}

	private static Object escapeCSV(Object cellValue) {
		if (cellValue == null)
			return "";
		String cellValueStr = cellValue.toString();
		return escapeEmbeddedCharacters(cellValueStr);
	}

	private static String escapeEmbeddedCharacters(String field) {
		StringBuffer buffer = null;
		final String separator = ",";
		// If the fields contents should be formatted to confrom with Excel's
		// convention....
		if (true) {

			// Firstly, check if there are any speech marks (") in the field;
			// each occurrence must be escaped with another set of spech marks
			// and then the entire field should be enclosed within another
			// set of speech marks. Thus, "Yes" he said would become
			// """Yes"" he said"
			if (field.contains("\"")) {
				buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
				buffer.insert(0, "\"");
				buffer.append("\"");
			} else {
				// If the field contains either embedded separator or EOL
				// characters, then escape the whole field by surrounding it
				// with speech marks.
				buffer = new StringBuffer(field);
				if ((buffer.indexOf(separator)) > -1
						|| (buffer.indexOf("\n")) > -1) {
					buffer.insert(0, "\"");
					buffer.append("\"");
				}
			}
			return (buffer.toString().trim());
		}
		// The only other formatting convention this class obeys is the UNIX one
		// where any occurrence of the field separator or EOL character will
		// be escaped by preceding it with a backslash.
		else {
			if (field.contains(separator)) {
				field = field.replaceAll(separator, ("\\\\" + separator));
			}
			if (field.contains("\n")) {
				field = field.replaceAll("\n", "\\\\\n");
			}
			return (field);
		}
	}

	public static InputStream openInputStream(String srcPath,
			java.util.List<ClassLoader> cls) {

		File file = new File(srcPath);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (Iterator iterator = cls.iterator(); iterator.hasNext();) {
			ClassLoader classLoader = (ClassLoader) iterator.next();
			URL url = classLoader.getResource(srcPath);
			if (url != null)
				try {
					InputStream is = url.openStream();
					if (is != null)
						return is;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (!srcPath.contains(":"))
			srcPath = "file:" + srcPath;
		try {
			return new URL(srcPath).openStream();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
