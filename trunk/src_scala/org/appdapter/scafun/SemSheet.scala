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

package org.appdapter.scafun

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

object SemSheet {
	val gdocSheetBaseURL = "https://docs.google.com/spreadsheet/pub";
	//  Building a param string like ?key=0ArBjkBo&single=true&gid=7&range=A2%3AK999&output=csv
	val gdocParamDocKey = "key";
	val gdocParamSingleSheet = "single";
	val gdocParamSheetNum = "gid";
	val gdocParamCellRange = "range";
	val gdocParamOutputFormat = "output";
	val gdocFormatCSV = "csv";
	val gdocFlagTrue = "true";
	val queryIndicator = "?";
	val paramSeperator = "&";
	val paramAssign = "=";
	
	val theDbg = new BasicDebugger();
	
	def makeParamBinding (name : String, v : String) = name + paramAssign + v;

	def makeGdocSheetQueryURL(docKey : String, sheetNum : Int, range : String) : String = {
		val builder = new StringBuilder(gdocSheetBaseURL)
		builder.append(queryIndicator);
		builder.append(makeParamBinding(gdocParamDocKey, docKey));
		builder.append(paramSeperator);
		builder.append(makeParamBinding(gdocParamSheetNum, sheetNum.toString));
		if (range != null) {
			builder.append(paramSeperator);
			builder.append(makeParamBinding(gdocParamCellRange, range));
		}
		builder.append(paramSeperator);
		builder.append(makeParamBinding(gdocParamOutputFormat, gdocFormatCSV));
		builder.append(paramSeperator);
		builder.append(makeParamBinding(gdocParamSingleSheet, gdocFlagTrue));
		
		builder.toString();
	}
	
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
	val	MT_Individual = "Individual";
	val MT_TypeProperty = "TypeProperty";
	val MT_ObjectProperty = "ObjectProperty";
	val MT_DatatypeProperty = "DatatypeProperty";
	
	class IndividualColProc {
		
	}
	

	class ModelInsertSheetProc (val myModel : Model) extends SheetProc(3) {
		var	myIndivColIdx = -1;
		override def absorbHeaderRows(headRows : Array[MatrixRow]) {
			val headRowLengths = headRows.map(_.getPossibleColumnCount());
			val headRowLengthMax = headRowLengths.max;
			val	propNameCells = headRows(0);
			val	metaKindCells = headRows(1);
			// Contains *either* RDF-Datatype  *or* default URI/QName prefix (applied when no ":" in data).
			val	subKindCells = headRows(2);
			
			val propResolver = new ResourceResolver(myModel, None);
			
			for (colIdx <- 0 until metaKindCells.getPossibleColumnCount()) {
				val metaKindCell : Option[String] = metaKindCells.getPossibleColumnValueString(colIdx);
				val propNameCell : Option[String] = propNameCells.getPossibleColumnValueString(colIdx);
				val subKindCell : Option[String] = subKindCells.getPossibleColumnValueString(colIdx);
				
				val optProp : Option[Property] =  propNameCell.map  { propResolver.findOrMakeProperty(myModel, _) }
			

				val optColBind = metaKindCell.map { _ match {
					case MT_Individual => {
						if (myIndivColIdx != -1) {
							throw new Exception("Got second column with MetaType='Individual' at col# " + colIdx)
						} else if (optProp.isDefined) {
							throw new Exception("Got illegal column with defined property " + optProp.get + " and MetaType='Individual' at col# " + colIdx)
						} else {
							myIndivColIdx = colIdx;
						}
						None;
					}
					case mt @ (MT_TypeProperty | MT_ObjectProperty) => {
						if (optProp.isEmpty) {
							throw new Exception("Got MetaType='" + mt + "' but no property name is specified at col# " + colIdx)
						} 
						val tgtResolver = new ResourceResolver(myModel, subKindCell);
						val binding = new ResLinkColumnBinding(optProp.get, colIdx, tgtResolver);
						Some(binding);
					}
					case mt @ MT_DatatypeProperty => {
						if (optProp.isEmpty) {
							throw new Exception("Got MetaType='" + mt + "' but no property name is specified at col# " + colIdx)
						}
						if (subKindCell.isEmpty) {
							throw new Exception("Got MetaType='" + mt + "' but no RDF-datatype is specified at col# " + colIdx)
						}
						val tm = TypeMapper.getInstance();
						// alternative:  getSafeTypeByName is more lenient
						val dt = tm.getTypeByName(subKindCell.get);
						val binding = new ResDataColumnBinding(optProp.get, colIdx, dt);
					}				
					case mt @ _ => {
						throw new Exception("Unknown MetaType in column " + colIdx + " : " + mt);
					}
				}}
			
			}
		}
	}
	

	

	
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
		
	abstract class PropertyValueColumnBinding(val myProp : Property, val myColIdx : Int) {
		def makeValueNode(cellString : String, model:Model) : RDFNode;
		def matrixCellToPossibleModelStmt(mtxRow: MatrixRow, modelParentRes:Resource) : Option[Statement] = {
			val optCellString : Option[String] = mtxRow.getPossibleColumnValueString(myColIdx);
			optCellString match {
				case None => None;
				case Some(cellString:String) => {
					val model : Model = modelParentRes.getModel();
					val rdfNode : RDFNode = makeValueNode(cellString, model);
					val stmt : Statement = model.createStatement(modelParentRes, myProp, rdfNode);
					model.add(stmt);
					Some(stmt);
				};
			}
		}
	}
	/**
	 * Binding for an input column of URIs / QNames.   optDefPrefix may be an abbrev OR a URI prefix ;  If it contains
	 * no colons, one is appended, making the input effectively, e.g. "xyz" -> "xyz:"
	 * 
	 * Ante ==> the prefix occurs before prefix-mapping resolution.
	 */
	class ResourceResolver(val myPrefixMap: PrefixMapping, val myOptDefAntePrefixWithOptColon : Option[String]) {
		val myPossDefAntePrefix : String = myOptDefAntePrefixWithOptColon match {
			case Some(defPrefix:String) => if (defPrefix.contains(":")) defPrefix else defPrefix + ":";
			case None => "";
		}

		def resolveURI(qnameOrURI:String) : String = {
			val qnOrURI : String = if (qnameOrURI.contains(":")) qnameOrURI else {myPossDefAntePrefix + qnameOrURI};
			val resolvedURI = myPrefixMap.expandPrefix(qnOrURI);
			resolvedURI;
		}
		def findOrMakeResource(model:Model, qnameOrURI:String) : Resource = {
			val uri = resolveURI(qnameOrURI);
			val res = model.createResource(uri);
			res;
		}
		// Jena low level API does not treat property and resource quite symmetrically.
		def findOrMakeProperty(model:Model, qnameOrURI:String) : Property = {
			val propRes = findOrMakeResource(model, qnameOrURI);
			val prop = propRes.as(classOf[Property]);
			prop;
		}
		
	}
	class ResLinkColumnBinding(linkProp : Property, colIdx: Int, val myResolver : ResourceResolver) 
			extends PropertyValueColumnBinding (linkProp, colIdx) {
				
		override def makeValueNode(cellString : String, model:Model) : RDFNode = {
			myResolver.findOrMakeResource(model, cellString);
		}
	}
	class ResDataColumnBinding(datProp : Property, colIdx: Int, val myDatatype : RDFDatatype) 
			extends PropertyValueColumnBinding (datProp, colIdx) {
				
		override def makeValueNode(cellString : String, model:Model) : RDFNode = {
			model.createTypedLiteral(cellString, myDatatype);
		}
	}
	
	def main(args: Array[String]) :Unit = {
	  	println("SemSheet test ");
		val keyForBootSheet22 = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc";
		val reposSheetNum = 8;
		val url = makeGdocSheetQueryURL(keyForBootSheet22, reposSheetNum, null);
		println("Made URL: " + url);
		val sp = new SheetProc(3);
		processSheet (url, sp.processRow);
	}	
}
