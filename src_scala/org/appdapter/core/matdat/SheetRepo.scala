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

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel}

import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, Syntax};
import com.hp.hpl.jena.query.{Dataset, DatasetFactory, DataSource};
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory};

import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}

import com.hp.hpl.jena.rdf.listeners.{ObjectListener};

import org.appdapter.bind.rdf.jena.model.{ModelStuff, JenaModelUtils};

import org.appdapter.core.store.{Repo, BasicQueryProcessorImpl, BasicRepoImpl};

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We implement a CSV (spreadsheet) backed Appdapter "repo" (read-only, but reloadable from updated source data).
 */



class RepoPrintinListener(val prefix: String) extends ObjectListener {
	 override def added(x : Object) : Unit = {
		 println(prefix + " added: " + x);
	 }
	 override def removed(x : Object) : Unit = {
		 println(prefix + " removed: " + x);
	 }
	 
}

class SheetRepo(val myDirectoryModel : Model) extends BasicRepoImpl {
	override def  makeMainQueryDataset() : Dataset = {
		val ds : Dataset = DatasetFactory.create() // becomes   createMem() in later Jena versions.
		ds;
	}
	override def  getGraphStats() : java.util.List[Repo.GraphStat] = new java.util.ArrayList();
	
	def loadSheetModelsIntoMainDataset() = {
		val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
		
		val nsJavaMap : java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
		
		val msqText = """
			select ?repo ?key ?sheet ?num 
				{
					?repo  a ccrt:GoogSheetRepo; ccrt:key ?key.
					?sheet a ccrt:GoogSheet; ccrt:sheetNumber ?num; ccrt:repo ?repo.
				}
		"""
		
		val msRset = QuerySheet.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);		
		import scala.collection.JavaConversions._;
		while (msRset.hasNext()) {
			val qSoln : QuerySolution = msRset.next();
			
			val repoRes : Resource = qSoln.getResource("repo");
			val sheetRes : Resource = qSoln.getResource("sheet");
			val sheetNum_Lit : Literal = qSoln.getLiteral("num")
			val sheetKey_Lit : Literal = qSoln.getLiteral("key")
			println("repoRes=" + repoRes + ", sheetRes=" + sheetRes + ", num=" + sheetNum_Lit + ", key=" + sheetKey_Lit)
			
			val sheetNum = sheetNum_Lit.getInt();
			val sheetKey = sheetKey_Lit.getString();
			val sheetModel : Model = SemSheet.readModelGDocSheet(sheetKey, sheetNum, nsJavaMap);
			println("Read sheetModel: " + sheetModel)
			val graphURI = sheetRes.getURI();
			mainDset.replaceNamedModel(graphURI, sheetModel)
		}

		
	}
}

object SheetRepo {
	def main(args: Array[String]) : Unit = {
		println("SheetRepo - start")
		val namespaceSheetNum = 9;
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(SemSheet.keyForBootSheet22, namespaceSheetNum, None);
		println("Made Namespace Sheet URL: " + namespaceSheetURL);
		// val namespaceMapProc = new MapSheetProc(1);
		// MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
		// namespaceMapProc.getJavaMap
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		
		println("Got NS map: " + nsJavaMap)		
		val directorySheetNum = 8;
		val directoryModel : Model = SemSheet.readModelGDocSheet(SemSheet.keyForBootSheet22, directorySheetNum, nsJavaMap);
			
		val sr = new SheetRepo(directoryModel)
		
		sr.loadSheetModelsIntoMainDataset()

	}
}