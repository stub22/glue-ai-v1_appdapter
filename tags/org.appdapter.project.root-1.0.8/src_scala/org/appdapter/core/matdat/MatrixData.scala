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

package org.appdapter.core.matdat

import java.io.Reader;
import java.util.Iterator;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;

import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode}
import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}
/**
 * @author Stu B. <www.texpedient.com>
 */

trait MatrixRow {
	def getPossibleColumnValueString(colIdx : Int) : Option[String];
	def getPossibleColumnCount() : Int;
	def dump() : String = {
		val rowLen = getPossibleColumnCount();
		val sbuf = new StringBuffer();
		for (colIdx <- 0 until rowLen) {
			val cellVal  : Option[String] = getPossibleColumnValueString(colIdx);
			if (colIdx > 0) {
				sbuf.append(", ");
			}
			sbuf.append(cellVal.getOrElse("[EMPTY]"));
		}
		sbuf.toString();
	}				
}
class MatrixRowCSV (val myRowArr : Array[String]) extends MatrixRow {
	val theDbg = new BasicDebugger();

	override def getPossibleColumnCount() : Int = myRowArr.length;
		
	override def getPossibleColumnValueString(colIdx : Int) : Option[String] = {
		if ((colIdx < 0) || (colIdx >= myRowArr.length)) {
			theDbg.logWarning("Column index " + colIdx + " is out of bounds for rowArray length " + myRowArr.length);
			None;
		} else {
			val	colVal : String = myRowArr(colIdx);
			if ((colVal == null) || (colVal.length() == 0)) {
				None;
			} else {
				Some(colVal);
			}
		}
	}

}
class SheetProc (val myHeaderRowCount : Int) {
	require(myHeaderRowCount > 0);
	private var myRowIdx = 0;
	private val myHeaderRows = new Array[MatrixRow](myHeaderRowCount);
		
	def processRow(mtxRow : MatrixRow) {
		if (myRowIdx < myHeaderRowCount) {
			myHeaderRows(myRowIdx) = mtxRow;
		} else {
			absorbDataRow(mtxRow);
		}
		myRowIdx += 1;
		if (myRowIdx == myHeaderRowCount) {
			absorbHeaderRows(myHeaderRows);
		}			
	}
	def absorbHeaderRows(headRows : Array[MatrixRow]) {
		for (hrIdx <- 0 until headRows.length) {
			println("HEADER[" + hrIdx + "] = " + headRows(hrIdx).dump());
		}
	}
	def absorbDataRow(cells : MatrixRow) {
		println("DATA = " + cells.dump());
	}
}
class MapSheetProc (headerRowCount : Int, val keyColIdx : Int, val vColIdx : Int) extends SheetProc(headerRowCount) {
	val myResultMap = scala.collection.mutable.Map.empty[String, String]
	
	override def absorbDataRow(cellRow : MatrixRow) {
		val key : Option[String] = cellRow.getPossibleColumnValueString(keyColIdx);
		val value : Option[String] = cellRow.getPossibleColumnValueString(vColIdx);
		
		if (key.isDefined && value.isDefined) {
			myResultMap.put(key.get, value.get)
		}
	}
	import collection.JavaConversions._	
	def getJavaMap : java.util.Map[String, String] = myResultMap;
}

object MatrixData {
	def processSheet(url : String, processor : MatrixRow => Unit) { 
		
		val rawReader : Reader = TestSheetReadMain.makeSheetDataReader(url);
		val csvr : CSVReader = new CSVReader(rawReader);
		
		var done = false;
		while (!done) {
			val rowArray  : Array[String] = csvr.readNext();
			val matrixRow = new MatrixRowCSV(rowArray);
			if (rowArray != null) {
				processor(matrixRow);
			} else {
				done = true;
			}
		}
		csvr.close();
		rawReader.close();
	}
	def readJavaMapFromSheet(sheetURL: String, headerCnt : Int = 1, keyColIdx : Int = 0, vlColIdx : Int = 1) : java.util.Map[String, String] = {
		val mapProc = new MapSheetProc(headerCnt, keyColIdx, vlColIdx);
		processSheet (sheetURL, mapProc.processRow);
		mapProc.getJavaMap
	}
}