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

package org.appdapter.fileconv;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.core.boot.ClassLoaderUtils;
import org.appdapter.core.log.Debuggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FileStreamUtils {

	@UISalient
	public static boolean SheetURLDataReaderMayReturnNullOnError = true;

	public static Reader makeSheetURLDataReader(String fullUrlTxt) throws IOException {
		try {
			return new InputStreamReader((new URL(fullUrlTxt)).openStream());
		} catch (Throwable t) {
			theLogger.error("Cannot read[" + fullUrlTxt + "]", t);
			if (SheetURLDataReaderMayReturnNullOnError)
				return null;
			throw Debuggable.reThrowable(t, IOException.class);
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(FileStreamUtils.class);

	public static Workbook getWorkbook(InputStream is, String extHint) throws IOException, InvalidFormatException {
		if (is == null)
			throw new IOException("Not input stream for hint: " + extHint);
		try {
			return WorkbookFactory.create(is);
		} catch (Exception e0) {
			if (extHint == null) {
				extHint = "xlsx";
			} else {
				extHint = extHint.toLowerCase();
			}
			try {
				if (extHint.endsWith("xlsx"))
					return new XSSFWorkbook(OPCPackage.open(is));
				if (extHint.endsWith("xls"))
					return new HSSFWorkbook(is);
				// openoffice documents
				return new XSSFWorkbook(OPCPackage.open(is));
			} catch (Exception e2) {
				return new HSSFWorkbook(is);
			}
		}
	}

	public static Workbook getWorkbook(String sheetLocation, java.util.List<ClassLoader> fileModelCLs) throws InvalidFormatException, IOException {
		InputStream stream = openInputStreamOrNull(sheetLocation, fileModelCLs);
		if (stream == null)
			throw new IOException("Location not found: " + sheetLocation);
		return getWorkbook(stream, getFileExt(sheetLocation));
	}

	public static Reader getSheetReaderAt(String sheetLocation, String sheetName, java.util.List<ClassLoader> fileModelCLs) {
		try {
			theLogger.info("getSheetReaderAt: " + sheetLocation + "!" + sheetName);
			return getWorkbookSheetCsvReaderAt(sheetLocation, sheetName, fileModelCLs);
		} catch (InvalidFormatException e) {
			theLogger.error("getWorkbookSheetCsvReaderAt ", e);
		} catch (IOException e) {
			theLogger.error("getWorkbookSheetCsvReaderAt ", e);
		}
		return null;
	}

	public static Model getModelIfAvailable(String sheetLocation, String sheetName, java.util.Map nsMap, java.util.List<ClassLoader> fileModelCLs) {
		FileManager fm = JenaFileManagerUtils.getDefaultJenaFM();

		for (ClassLoader cl : fileModelCLs)
			fm.addLocatorClassLoader(cl);

		Model m = getModelIfAvailable(sheetLocation + sheetName, fm);
		if (m != null)
			return m;
		m = getModelIfAvailable(sheetName, fm);
		if (m != null)
			return m;
		try {
			return fm.loadModel(sheetName, sheetLocation);
		} catch (Exception e) {
			return null;
		}
	}

	public static Model getModelIfAvailable(String sheetLocation, FileManager fm) {
		try {
			return fm.loadModel(sheetLocation);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isNullOrEmptyString(CharSequence str) {
		return str == null || str.length() == 0;
	}

	public static String[] splitOfSubPath(String str) {
		String[] splt = str.split("#");
		if (splt.length == 2)
			return splt;
		return str.split("!");
	}

	public static String combineLocationAndSheet(String sheetLocation, String sheetName) {
		boolean missingSheetLocation = isNullOrEmptyString(sheetLocation);
		boolean missingSheetName = isNullOrEmptyString(sheetName);
		if (missingSheetLocation && missingSheetName) {
			return null;
		}
		if (missingSheetName)
			return sheetLocation;
		if (missingSheetLocation)
			return sheetName;
		String combined = sheetLocation + "!" + sheetName;
		return combined;
	}

	public static Reader getWorkbookSheetCsvReaderAt(String sheetLocation, String sheetName, java.util.List<ClassLoader> fileModelCLs) throws InvalidFormatException, IOException {
		boolean missingSheetLocation = isNullOrEmptyString(sheetLocation);
		boolean missingSheetName = isNullOrEmptyString(sheetName);
		if (missingSheetLocation && missingSheetName) {
			return NotFound("NULL SheetReader Location");
		}
		String combined = combineLocationAndSheet(sheetLocation, sheetName);
		Workbook workbook = getWorkbook(sheetLocation, fileModelCLs);
		if (workbook == null) {
			InputStream is = openInputStreamOrNull(sheetName, fileModelCLs);
			if (is == null)
				is = openInputStreamOrNull(combined, fileModelCLs);
			if (is == null)
				return NotFound(combined);

			String ext = getFileExt(sheetName);
			if (ext != null && ext.endsWith("csv")) {
				return new InputStreamReader(is);
			}
			workbook = getWorkbook(is, ext);
			if (workbook == null)
				return NotFound(sheetLocation + sheetName);
			return sheetToReader(workbook.getSheetAt(0));
		}
		// i know this is dead code but it might not be soon
		if (workbook == null)
			return NotFound(sheetLocation + sheetName);
		int sheetNumber = workbook.getSheetIndex(sheetName);
		Sheet sheet = null;
		if (sheetNumber >= 0)
			sheet = workbook.getSheetAt(sheetNumber);
		// use the workbook API
		if (sheet != null)
			return sheetToReader(sheet);
		Sheet sheet2 = null;
		int max = workbook.getNumberOfSheets() - 1;
		String sheetNameS = matchableName(sheetName);
		for (sheetNumber = 0; sheetNumber <= max; sheetNumber++) {
			sheet = workbook.getSheetAt(sheetNumber);
			String sn = matchableName(sheet.getSheetName());
			// found it by name
			if (sheetNameS.equals(sn))
				return sheetToReader(sheet);
			// cases like "Nspc.csv"
			if (sheetNameS.startsWith(sn))
				sheet2 = sheet;
		}
		if (sheet2 != null)
			return sheetToReader(sheet2);
		return NotFound(sheetLocation + sheetName);
	}

	public static String matchableName(String sheetName) {
		if (sheetName == null)
			return "";
		return (sheetName + " ").replace(".csv ", "").replace(".xlsx ", "").replaceAll("-", "").replaceAll(" ", "").toLowerCase();
	}

	private static Reader NotFound(String string) throws IOException {
		throw new FileNotFoundException(string);
	}

	public static String getFileExt(String srcPath) {
		int at = srcPath.lastIndexOf('.');
		if (at < 0)
			return null;
		return srcPath.substring(at + 1).toLowerCase();
	}

	public static Reader sheetToReader(Sheet sheet) {
		String str = sheetToString(sheet);
		String sn = sheet.getSheetName();
		getLogger().debug("Reading Sheet " + sn + " " + str.length() + " bytes");
		saveFileString(sn, str);
		return new StringReader(str);
	}

	public static void saveFileString(String sn) {
		URL url;
		try {
			url = new URL(sn);
			InputStream is = url.openStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			int result = bis.read();
			while (result != -1) {
				byte b = (byte) result;
				buf.write(b);
				result = bis.read();
			}

			saveFileString(sn.replaceAll(":", "-").replaceAll("/", "-").replaceAll(".", "-").replaceAll("?", "-").replaceAll("=", "-").replaceAll("--", "-"), buf.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused") private static void saveFileString(String sn, String str) {
		if (true)
			return;
		try {
			FileWriter fw = new FileWriter(new File(matchableName(sn) + ".csv"));
			fw.write(str);
			fw.close();
		} catch (IOException e) {
			getLogger().error(" Saving Sheet " + sn + " { \n " + str + "\n }", e);
			e.printStackTrace();
		}
	}

	private static Logger getLogger() {
		return theLogger;
	}

	public static String sheetToString(Sheet sheet) {
		StringBuffer sheetBuff = new StringBuffer();
		if (sheet.getPhysicalNumberOfRows() == 0) {
			throw new RuntimeException("No rows on sheet: " + sheet);
		}
		int width = getSheetWidth(sheet);
		int maxInclusive = sheet.getLastRowNum();
		for (int i = sheet.getFirstRowNum(); i <= maxInclusive; i++) {
			Row row = sheet.getRow(i);
			if (row == null)
				continue;
			int rwInclusve = row.getLastCellNum();
			String str = getRowString(row, width);
			StringBuffer strBuff = new StringBuffer(str);
			// pad the rest
			int pad = width - rwInclusve;
			for (int j = 0; j < pad; j++) {
				strBuff.append(",");
			}
			sheetBuff.append(strBuff.toString().trim() + "\n");
		}
		return sheetBuff.toString();
	}

	private static String getRowString(Row row, int width) {
		return getRowString(row, width, false);
	}

	private static String getRowDebugString(Row row, int width) {
		return getRowString(row, width, true);
	}

	private static String getRowString(Row row, int width, boolean debugString) {
		int rwInclusve = row.getLastCellNum();
		if (rwInclusve > width)
			rwInclusve = width;
		StringBuffer strBuff = new StringBuffer();
		if (debugString) {
			strBuff.append("##;; " + row.getSheet().getSheetName() + " rownum= " + row.getRowNum() + "\n\n");
		}

		for (int j = 0; j <= rwInclusve; j++) {
			Cell cell = row.getCell(j);
			if (j > 0)
				strBuff.append(",");

			if (cell == null)
				continue;
			String t, c;
			String s = null;

			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				continue;
			case Cell.CELL_TYPE_STRING: {
				c = cell.getStringCellValue();
				strBuff.append(escapeCSV(c));
				continue;
			}

			case Cell.CELL_TYPE_NUMERIC: {
				t = "CELL_TYPE_NUMERIC";
				c = ("" + cell.getNumericCellValue() + " ").replace(".0 ", "").trim();
				break;
			}
			case Cell.CELL_TYPE_FORMULA: {
				t = "CELL_TYPE_FORMULA";
				try {
					c = cell.getCellFormula();
				} catch (org.apache.poi.ss.formula.FormulaParseException e) {					
					if (Debuggable.isRelease())
						theLogger.warn("" + e);
					else {
						theLogger.error("" + e, e);
					}
					cell.setCellType(Cell.CELL_TYPE_STRING);
					c = cell.getStringCellValue();
				}
				break;
			}
			case Cell.CELL_TYPE_BOOLEAN: {
				t = "CELL_TYPE_BOOLEAN";
				c = "" + cell.getBooleanCellValue();
				break;
			}
			case Cell.CELL_TYPE_ERROR: {
				t = "CELL_TYPE_ERROR";
				c = "" + cell.getErrorCellValue();
				break;
			}
			default: {
				t = "CELL_TYPE_" + cell.getCellType();
				c = cell.getStringCellValue();
				break;
			}
			}
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s = cell.getStringCellValue();
			if (s != null && s.length() > 1) {
				strBuff.append(escapeCSV(s));
				continue;
			}

			if (s == null || s.length() < 1) {
				if (!debugString) {
					String msg = Debuggable.toInfoStringArgV(t + " really? " + c, "cell=" + cell, "cellAsString=" + s, //
							"row.getClass= " + row.getClass(), "sheet=" + cell.getSheet().getSheetName(), //
							"row=" + cell.getRow(), "rowstr = " + getRowDebugString(row, width));
					if (!Debuggable.isRelease())
						Debuggable.doBreak(msg);
				}
			}
			c = s;
			strBuff.append(escapeCSV(c));
			continue;
		}
		return strBuff.toString();
	}

	private static int getSheetWidth(Sheet sheet) {
		// if (true) return 0;
		Row row = sheet.getRow(sheet.getFirstRowNum());
		int hadStuff = -1;
		for (int i = 0; i < row.getLastCellNum(); i++) {
			Cell c = row.getCell(i);
			if (c == null)
				continue;
			if (c.getCellType() == Cell.CELL_TYPE_STRING)
				hadStuff = i;
		}
		return hadStuff;
	}

	private static Object escapeCSV(Object cellValue) {
		if (cellValue == null)
			return "";
		String cellValueStr = cellValue.toString();
		return escapeEmbeddedCharacters(cellValueStr);
	}

	private static String escapeEmbeddedCharacters(String field) {
		if (field == null)
			return "";
		field = field.replace("\r\n", "\n").replace("\r", "\n").replace("\n", " ").trim();
		if (field.length() == 0)
			return field;

		// If the fields contents should be formatted to conform with Excel's
		// convention....

		// Firstly, check if there are any speech marks (") in the field;
		// each occurrence must be escaped with another set of speech marks
		// and then the entire field should be enclosed within another
		// set of speech marks. Thus, "Yes" he said would become
		// """Yes"" he said"
		if (field.contains("\"") || field.contains("\n") || field.contains(",")) {
			return "\"" + field.replaceAll("\"", "\"\"") + "\"";
		}
		return field;
	}

	public static InputStream openInputStreamOrNull(String srcPath, java.util.List<ClassLoader> cls) {
		try {
			return openInputStream(srcPath, cls);
		} catch (Throwable e) {
			getLogger().error("Bad srcPath={}", srcPath, e);
			return null;
		}
	}

	public static InputStream openInputStream(String srcPath, java.util.List<ClassLoader> cls) throws IOException {

		if (cls == null) {
			cls = ClassLoaderUtils.getCurrentClassLoaderList();
		}
		if (srcPath == null)
			throw new MalformedURLException("URL = NULL");
		IOException ioe = null;
		File file = new File(srcPath);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (IOException io) {
				// It existed so this might be legit
				ioe = io;
			}
		}
		if (srcPath.contains(":")) {
			try {
				return new URL(srcPath).openStream();
			} catch (MalformedURLException maf) {
				if (ioe == null)
					ioe = maf;
			} catch (IOException e) {
				ioe = e;
			}
		}
		for (Iterator iterator = cls.iterator(); iterator.hasNext();) {
			ClassLoader classLoader = (ClassLoader) iterator.next();
			InputStream is = null;
			URL url = classLoader.getResource(srcPath);
			if (url != null) {
				try {
					is = url.openStream();
				} catch (IOException e) {
					ioe = e;
				}
			} else {
				is = classLoader.getResourceAsStream(srcPath);
			}
			if (is != null)
				return is;
		}

		if (!srcPath.contains(":")) {
			InputStream is = ClassLoader.getSystemResourceAsStream(srcPath);
			if (is != null)
				return is;
			srcPath = "file:" + srcPath;
			try {
				return new URL(srcPath).openStream();
			} catch (MalformedURLException maf) {
				if (ioe == null)
					ioe = maf;
			} catch (IOException e) {
				ioe = e;
			}
			if (ioe != null) {
				throw ioe;
			}
		}

		return null;
	}
}
