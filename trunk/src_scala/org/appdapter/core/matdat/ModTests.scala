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
import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory, QuerySolution};

import com.hp.hpl.jena.rdf.listeners.{ObjectListener};

import org.appdapter.bind.rdf.jena.model.{ModelStuff, JenaModelUtils};
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We test the processing of Jena model listeners, which receive messages for groups of statements added/removed,
 * in combination with a Jena Model's basic transaction commit/rollback capability. 
 */



class PrintinListener(val prefix: String) extends ObjectListener {
	 override def added(x : Object) : Unit = {
		 println(prefix + " added: " + x);
	 }
	 override def removed(x : Object) : Unit = {
		 println(prefix + " removed: " + x);
	 }
	 
}


object ModTests {
	def main(args: Array[String]) : Unit = {
		println("ModTests - start")
		val namespaceSheetNum = 9;
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(SemSheet.keyForBootSheet22, namespaceSheetNum, None);
		println("Made Namespace Sheet URL: " + namespaceSheetURL);
		// val namespaceMapProc = new MapSheetProc(1);
		// MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
		// namespaceMapProc.getJavaMap
		val nsJavaMap : java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
		
		println("Got NS map: " + nsJavaMap)		
		val reposSheetNum = 8;
		val reposModel : Model = SemSheet.readModelGDocSheet(SemSheet.keyForBootSheet22, reposSheetNum, nsJavaMap);
		

	
		val prinListener = new PrintinListener("underlying repo model");
/* ModelCom 
 http://grepcode.com/file/repo1.maven.org/maven2/com.hp.hpl.jena/jena/2.6.3/com/hp/hpl/jena/rdf/model/impl/ModelCom.java#ModelCom.register
 
 * public Model register( ModelChangedListener listener )
1302        getGraph().getEventManager().register( adapt( listener ) );

http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/graph/Graph.html

public interface Graph extends GraphAdd
The interface to be satisfied by implementations maintaining collections of RDF triples. 
The core interface is small (add, delete, find, contains) and is augmented by additional 
classes to handle more complicated matters such as reification, query handling, bulk update, 
event management, and transaction handling.

 */		
		reposModel.register(prinListener);
		
		val emptyModel = ModelFactory.createDefaultModel();
		val unityModel = ModelFactory.createUnion(reposModel, emptyModel);		
		
		
		
		val rdfsReposModel : InfModel = ModelFactory.createRDFSModel(reposModel);
		
		val repInfListener = new PrintinListener("RDFS-inferred repo model");
		
		rdfsReposModel.register(repInfListener)
		
		val deductionsModel : Model = rdfsReposModel.getDeductionsModel();
		
		val deductionsListener = new PrintinListener("Deductions model");
		
		val queriesSheetNum = 12;
		val queriesModel : Model = SemSheet.readModelGDocSheet(SemSheet.keyForBootSheet22, queriesSheetNum, nsJavaMap);		
		
		val tqText = "select ?sheet { ?sheet a ccrt:GoogSheet }";
		
		val trset = QueryHelper.execModelQueryWithPrefixHelp(reposModel, tqText);
		val trxml = QueryHelper.buildQueryResultXML(trset);
		
		println("Got repo-query-test result-XML: \n" + trxml);
		
		val qqText = "select ?qres ?qtxt { ?qres a ccrt:SparqlQuery; ccrt:queryText ?qtxt}";

		val qqrset : ResultSet = QueryHelper.execModelQueryWithPrefixHelp(queriesModel, qqText);
		val qqrsrw = ResultSetFactory.makeRewindable(qqrset);
		// Does not disturb the original result set
		val qqrxml = QueryHelper.buildQueryResultXML(qqrsrw);

		import scala.collection.JavaConversions._;	
			
		
		println("Got query-query-test result-XML: \n" + qqrxml);
		qqrsrw.reset();
		val allVarNames : java.util.List[String] = qqrsrw.getResultVars();
		println ("Got all-vars java-list: " + allVarNames);
		while (qqrsrw.hasNext()) {
			val qSoln : QuerySolution = qqrsrw.next();
			for (val n : String <- allVarNames ) {
				val qvNode : RDFNode = qSoln.get(n);
				println ("qvar[" +  n + "]=" + qvNode);
			}
			
			val qtxtLit : Literal = qSoln.getLiteral("qtxt")
			val qtxtString = qtxtLit.getString();
			val zzRset = QueryHelper.execModelQueryWithPrefixHelp(reposModel, qtxtString);
			val zzRSxml = QueryHelper.buildQueryResultXML(zzRset);
			println ("Query using qTxt got: " + zzRSxml)
			
	//		logInfo("Got qsoln" + qSoln + " with s=[" + qSoln.get("s") + "], p=[" + qSoln.get("p") + "], o=[" 
	//						+ qSoln.get("o") +"]");
		}
		
		val repoGraph = reposModel.getGraph()
		println("reposModel Graph [class=" + repoGraph.getClass().getName() + "]  : " + repoGraph)
		reposModel.add(queriesModel);
		println("Finished first add")
/**
 * Finished first add
Exception in thread "main" java.lang.UnsupportedOperationException: this model does not support transactions
	at com.hp.hpl.jena.graph.impl.SimpleTransactionHandler.notSupported(SimpleTransactionHandler.java:30)
	at com.hp.hpl.jena.graph.impl.SimpleTransactionHandler.begin(SimpleTransactionHandler.java:21)
	at com.hp.hpl.jena.rdf.model.impl.ModelCom.begin(ModelCom.java:1083)
	at org.appdapter.core.matdat.ModTests$.main(ModTests.scala:102)
	at org.appdapter.core.matdat.ModTests.main(ModTests.scala)

		reposModel.begin()
		println("Finished xact-begin")
 */				
/*
 * Added: <ModelCom   {ccrt:find_sheets_99 @rdf:type ccrt:NeedsPrefixHelp; ccrt:find_sheets_99 @ccrt:queryText "select ?sheet { ?sheet a ccrt:GoogSheet }"; ccrt:find_sheets_99 @rdf:type ccrt:SparqlQuery} | >
Finished first add
Added: <ModelCom   {ccrt:find_sheets_99 @rdf:type ccrt:NeedsPrefixHelp; ccrt:find_sheets_99 @ccrt:queryText "select ?sheet { ?sheet a ccrt:GoogSheet }"; ccrt:find_sheets_99 @rdf:type ccrt:SparqlQuery} | >
Finished second add
http://grepcode.com/file/repo1.maven.org/maven2/com.hp.hpl.jena/jena/2.6.3/com/hp/hpl/jena/graph/impl/GraphBase.java#200
		 
		 196	public void add( Triple t ) 
197        {
198        checkOpen();
199        performAdd( t );
200        notifyAdd( t );
201        }
    public final void delete( Triple t )
218        {
219        checkOpen();
220        performDelete( t );
221        notifyDelete( t );
222        }
        
    

		 public abstract class GraphMemBase extends GraphBase
		    public final TripleStore store;

		 17 public class GraphMemFaster extends GraphMemBase
20         { this( ReificationStyle.Minimal ); }		
31     @Override public void performAdd( Triple t )
32         { if (!getReifier().handledAdd( t )) store.add( t ); }
34     @Override public void performDelete( Triple t )
35         { if (!getReifier().handledRemove( t )) store.delete( t ); }
25     @Override protected TripleStore createTripleStore()
26         { return new FasterTripleStore( this ); }		 
 */
		reposModel.add(queriesModel);
		println("Finished second add")
		val wackyStmtText01 =  " a B c ; d E f "
		println("Parsing statements [" + wackyStmtText01 + "]")
		val sArray : Array[Statement] = ModelStuff.statements(reposModel, wackyStmtText01 ) 
			
/*
 * Parsed statements: 
Found stmt: [eh:/a, eh:/B, eh:/c]
Found stmt: [eh:/d, eh:/E, eh:/f]
--------------------------------------------
 * 
 */		
		sArray foreach (x => {			
			println ("Found stmt: " + x)
			reposModel.add(x)
			None
		})
		println("Finished third add")

		reposModel.add(sArray)
		

		println("Finished fourth add")
		println("InfModel[class=" + rdfsReposModel.getClass().getName() + ", size=" + rdfsReposModel.size() + "]= " + rdfsReposModel)
		rdfsReposModel.listStatements() foreach (stmt => {println("[i-stmt] " + stmt.toString())})
		println("---------------- Hmm -----------------")
		println("UnderModel[class=" + reposModel.getClass().getName() + ", size=" + reposModel.size() + "]= " + reposModel)
		reposModel.listStatements() foreach (stmt => {println("[u-stmt] " + stmt.toString())})
		println("---------------- Well -----------------")
		println("DeductionsModel[class=" + deductionsModel.getClass().getName() + ", size=" + deductionsModel.size() + "]= " + deductionsModel)
		deductionsModel.listStatements() foreach (stmt => {println("[d-stmt] " + stmt.toString())})
	}
	
}
