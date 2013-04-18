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

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.ss.usermodel.*;
import org.openjena.atlas.io.InputStreamBuffered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FileStreamUtils {
	
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
			theLogger.info("getSheetReaderAt: " + sheetLocation + " @ " + sheetName);
			return getSheetReaderAtCanThrow(sheetLocation, sheetName, fileModelCLs);
		} catch (InvalidFormatException e) {
			theLogger.error("getSheetReaderAtCanThrow ", e);
		} catch (IOException e) {
			theLogger.error("getSheetReaderAtCanThrow ", e);
		}
		return null;
	}
	
	public static boolean doBreak(Object... s) {
		
		PrintStream v = System.out;
		new Exception("" + s[0]).fillInStackTrace().printStackTrace(v);
		for (int i = 0; i < s.length; i++) {
			getLogger().error("" + s[i]);
		}
		if (true) return false;
		getLogger().info("Press enter to continue");
		System.console().readLine();
		return true;
	}
	
	@SuppressWarnings("unused")
	public static Reader getSheetReaderAtCanThrow(String sheetLocation, String sheetName, java.util.List<ClassLoader> fileModelCLs) throws InvalidFormatException, IOException {
		Workbook workbook = getWorkbook(sheetLocation, fileModelCLs);
		if (workbook == null) {
			InputStream is = openInputStreamOrNull(sheetName, fileModelCLs);
			if (is == null)
				is = openInputStreamOrNull(sheetLocation + sheetName, fileModelCLs);
			if (is == null)
				return NotFound(sheetLocation + sheetName);
			
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
	
	@SuppressWarnings("unused")
	private static void saveFileString(String sn, String str) {
		if (true) return;
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
					c = cell.getCellFormula();
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
					doBreak(t + " really? " + c, "cell=" + cell, "cellAsString=" + s, "row.getClass= " + row.getClass(), "sheet=" + cell.getSheet().getSheetName(), "row=" + cell.getRow(), "rowstr = " + getRowDebugString(row, width));
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
		
		// If the fields contents should be formatted to confrom with Excel's
		// convention....
		
		// Firstly, check if there are any speech marks (") in the field;
		// each occurrence must be escaped with another set of spech marks
		// and then the entire field should be enclosed within another
		// set of speech marks. Thus, "Yes" he said would become
		// """Yes"" he said"
		if (field.contains("\"") || field.contains("\n") || field.contains(",")) {
			return "\"" + field.replaceAll("\"", "\"\"") + "\"";
		}
		return field;
	}
	
	@SuppressWarnings("rawtypes")
	public static InputStream openInputStreamOrNull(String srcPath, java.util.List<ClassLoader> cls) {
		
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
