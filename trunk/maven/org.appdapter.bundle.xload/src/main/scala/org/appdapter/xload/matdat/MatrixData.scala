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

package org.appdapter.xload.matdat

import java.io.{ FileNotFoundException, Reader }

import org.appdapter.core.log.{ BasicDebugger, Debuggable }

import au.com.bytecode.opencsv.CSVReader
/**
 * @author Stu B. <www.texpedient.com>
 */

trait MatrixRow {
  def getPossibleColumnValueString(colIdx: Int): Option[String];
  def getPossibleColumnCount(): Int;
  def dump(): String = {   
    val rowLen = getPossibleColumnCount();
    val sbuf = new StringBuffer();
    for (colIdx <- 0 until rowLen) {
      Debuggable.putFrameVar("column", colIdx)
      val cellVal: Option[String] = getPossibleColumnValueString(colIdx);
      if (colIdx > 0) {
        sbuf.append(", ");
      }
      sbuf.append(cellVal.getOrElse("[EMPTY]"));
    }
    sbuf.toString();
  }
}
class MatrixRowCSV(val myRowArr: Array[String]) extends MatrixRow {
  val theDbg = new BasicDebugger();

	override def getPossibleColumnCount(): Int = myRowArr.length;

	override def getPossibleColumnValueString(colIdx: Int): Option[String] = {
		if ((colIdx < 0) || (colIdx >= myRowArr.length)) {
			theDbg.logWarning("Column index " + colIdx + " is out of bounds for rowArray length " + myRowArr.length)
			None
		} else {
			val colVal: String = myRowArr(colIdx);
			if (colVal != null) {
				val trimmedVal = colVal.trim
				if (trimmedVal.length > 0) {
					Some(trimmedVal)
				} else None
			} else None
		}
	}
}


class SheetProc(val myHeaderRowCount: Int) extends BasicDebugger {
  require(myHeaderRowCount > 0);
  private var myRowIdx = 0;
  private val myHeaderRows = new Array[MatrixRow](myHeaderRowCount);

  def processRow(mtxRow: MatrixRow) {
    Debuggable.putFrameVar("row", myRowIdx + 1)
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
  def absorbHeaderRows(headRows: Array[MatrixRow]) {
    for (hrIdx <- 0 until headRows.length) {
      getLogger.debug("HEADER[" + hrIdx + "] = " + headRows(hrIdx).dump());
    }
  }
  def absorbDataRow(cells: MatrixRow) {
    getLogger.error("DATA = " + cells.dump());
  }
}
/**
 * Used to read a simple two column sheet into a (key,value) pair map.
 */
class MapSheetProc(headerRowCount: Int, val keyColIdx: Int, val vColIdx: Int) extends SheetProc(headerRowCount) {
  val myResultMap = new java.util.HashMap[String, String]()

  override def absorbDataRow(cellRow: MatrixRow) {
    val key: Option[String] = cellRow.getPossibleColumnValueString(keyColIdx);
    val value: Option[String] = cellRow.getPossibleColumnValueString(vColIdx);

    if (key.isDefined && value.isDefined) {
      val rowIsCommentedOut: Boolean = key.get.trim.startsWith("#");
      if (!rowIsCommentedOut) {
        val k = key.get;
        val v = value.get;
        myResultMap.put(k, v)
      } else {
        getLogger.debug("Row is commented out: " + cellRow.dump());
      }
    }
  }
  //import collection.JavaConversions._	
  def getJavaMap: java.util.Map[String, String] = myResultMap;
}

object MatrixData extends BasicDebugger {

  def processSheet(url: String, processor: MatrixRow => Unit) {

    val rawReader: Reader = org.appdapter.fileconv.FileStreamUtils.makeSheetURLDataReader(url);
    if (rawReader == null) {
      getLogger().error("No sheet found: " + url, new FileNotFoundException(url))
    } else {
      Debuggable.putFrameVar("sheetURL", url)
      processSheetR(rawReader, processor);
    }
  }

  def processSheetR(rawReader: Reader, processor: MatrixRow => Unit) {
    if (rawReader == null) {
      getLogger().error("NUll reader")
      return
    }
    val csvr: CSVReader = new CSVReader(rawReader);

    var done = false;
    while (!done) {
      val rowArray: Array[String] = csvr.readNext();
      if (rowArray != null) {
        try {
          val matrixRow = new MatrixRowCSV(rowArray);
          processor(matrixRow);
        } catch {
          case e: Exception => getLogger().error(Debuggable.toInfoStringArgV("processing a row problem " + e, e, processor, rowArray))
        }
      } else {
        done = true;
      }
    }
    csvr.close();
    rawReader.close();
  }
  def readJavaMapFromSheet(sheetURL: String, headerCnt: Int = 1, keyColIdx: Int = 0, vlColIdx: Int = 1): java.util.Map[String, String] = {
    val mapProc = new MapSheetProc(headerCnt, keyColIdx, vlColIdx);
    Debuggable.putFrameVar("sheetURL", sheetURL)
    processSheet(sheetURL, mapProc.processRow);
    mapProc.getJavaMap
  }

  def readJavaMapFromSheetR(rawReader: Reader, headerCnt: Int = 1, keyColIdx: Int = 0, vlColIdx: Int = 1): java.util.Map[String, String] = {
    val mapProc = new MapSheetProc(headerCnt, keyColIdx, vlColIdx);
    processSheetR(rawReader, mapProc.processRow);
    mapProc.getJavaMap
  }
}
