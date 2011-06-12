/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

/*
 *  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  See important notices at bottom of this file.
 */

package com.appdapter.peru.test.binding.jena;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import java.io.StringWriter;

import com.appdapter.peru.binding.jena.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.sparql.core.DataFormat;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX;
import com.hp.hpl.jena.sparql.junit.TestQueryUtils;
import com.hp.hpl.jena.sparql.junit.QueryTestException;
import com.hp.hpl.jena.util.junit.TestUtils;

import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.sparql.junit.TestItem;

import junit.framework.TestCase;

/**
 * PeruserARQ_Harness is a testing wrapper around the Jena ARQ query engine for SPARQL.
 *
 * <BR/>
 * Much of this class is based on the Jena ARQ JUnit query test harness in com/hp/hpl/jena/query/junit/QueryTest.java.
 * The HP copyright notice and licensing terms under which this code is redistrubted are at the bottom of this file.
 * 
 * <BR/>
 * We have added:
 * <UL>
 * <LI>Support for configurable inference on the query model inputs.</LI>
 * </UL>
 *
 * @author      Stu B. <www.texpedient.com> of Scrutable, based on code by Andy Seaborne of HP.
 * @version     @PERUSER_VERSION@
 * @copyright   derivative(HP, Scrutable)
 */
public class PeruserARQ_Harness {
    private static Log 		theLog = LogFactory.getLog(PeruserARQ_Harness.class );
	
    // private		Model 					myInputModel;
    private		Model 					myExpectedResultsModel = null ;     // Maybe null if no testing of results
	
    private static boolean  			thePrintModelsOnFailureFlag = true;	
	
	private		Dataset					myInputDataset = null;
	private		Query					myParsedQuery = null;

    private		TestItem				myARQTestItem ;
	private		PeruserARQTestItem		myPeruserTestItem;

    private		FileManager 			myQueryFileManager ;
    private		boolean 				my_isRDQLtestFlag = false ;
    private 	boolean 				myResetNeededFlag = false ;

	private		String					myTestName;
	
	private 	String					myManifestEntryURI;
	
	private		QueryExecution			myQueryExecution;
	
	private		ResultSetRewindable		myLastResultSet;
	private		Model					myLastResultModel;
	private		String					myLastResultXML;
	private		Boolean					myLastResultFlag;
	
	private		Reasoner				myReasoner;
	
	
    /**
	 * If supplied with a model, the test will load that model with data from the source
     * If no model is supplied one is created or attached (e.g. a database)
	 */
    PeruserARQ_Harness(String manifestEntryURI, Dataset inputDataset, String testName, FileManager fm, PeruserARQTestItem t) {
		myTestName = fixName(testName);
		myManifestEntryURI = manifestEntryURI;
        // myInputModel = m ;
		myInputDataset = inputDataset;
        myQueryFileManager = fm;
		myPeruserTestItem = t;

        myARQTestItem = t.getARQTestItem();
        my_isRDQLtestFlag = (myARQTestItem.getQueryFileSyntax().equals(Syntax.syntaxRDQL));
    }
	public static PeruserARQ_Harness makeFromManifestConfig(String manifestEntryURI, Dataset inputDataset, FileManager fm, 
				Resource manifest, Resource entry, String testName, Resource action, Resource result)
    {
		PeruserARQ_Harness harness = null ;
        // Defaults.
        Syntax querySyntax = TestQueryUtils.getQuerySyntax(manifest);
        if ( querySyntax != null) {
			theLog.info("Explicit querySyntax specified: " + querySyntax);
            if (!querySyntax.equals(Syntax.syntaxRDQL) && !querySyntax.equals(Syntax.syntaxARQ) &&
							!querySyntax.equals(Syntax.syntaxSPARQL) ) {
                throw new QueryTestException("Unknown syntax: "+querySyntax) ;
			}
        }
        // May be null
        Resource defaultTestType = TestUtils.getResource(manifest, TestManifestX.defaultTestType) ;
        // test name, test type, action -> query specific query[+data], results
        PeruserARQTestItem item = new PeruserARQTestItem(entry, defaultTestType, querySyntax, DataFormat.langXML);
		Resource  itt = item.getARQTestItem().getTestType();
		// System.out.println("ItemTestType for " + testName + " is " + itt);
		if ((itt == null) || (itt.equals(TestManifestX.TestQuery))) {
			harness = new PeruserARQ_Harness(manifestEntryURI, inputDataset, testName, fm, item);
		} else {
			theLog.warn("Ignoring non-query test " + testName + " of type " + itt);
		}
        return harness;
    }
	
	public void clearResults() {
		myLastResultSet = null; myLastResultModel = null; myLastResultXML = null; myLastResultFlag=null;
	}
	public ResultSetRewindable getLastResultSet() {
		return myLastResultSet;
	}
	public Model getLastResultModel() {
		return myLastResultModel;
	}
	public Boolean getLastResultFlag() {
		return myLastResultFlag;
	}
	public String getLastResultXML() {
		return myLastResultXML;
	}

	protected void setLastResultSet (ResultSet rs) {
		myLastResultSet = ResultSetFactory.makeRewindable(rs);
		myLastResultXML = ResultSetFormatter.asXMLString(myLastResultSet);
		myLastResultSet.reset();
	}
	protected void setLastResultFlag (boolean rf) {
		myLastResultFlag = new Boolean(rf);
		myLastResultXML = ResultSetFormatter.asXMLString(rf);
	}
	protected void setLastResultModel (Model rm) {
		// Client can fetch this as N3/TTL/XML...
		myLastResultModel = rm;
		StringWriter resultModelDumpStringWriter = new StringWriter();
		rm.write(resultModelDumpStringWriter);
		myLastResultXML = resultModelDumpStringWriter.toString();
	}

	synchronized void execPrepare() throws Throwable {
		clearResults();
		
		myParsedQuery = myPeruserTestItem.parseQueryFile();
		
		// Added by Stu, 2006-01-16
		parseRulesFile();
		
		myQueryExecution = makeQueryExecution();
	}
	
	synchronized void execQuery() throws Throwable {
		// verifySingleDataset();                      
		Query q = getParsedQuery();
		QueryExecution qe = myQueryExecution;
	
		if (q.isSelectType() ) {
			ResultSet rs = qe.execSelect();
			setLastResultSet (rs);
		} else if (q.isConstructType()) {
			Model rm = qe.execConstruct() ;
			setLastResultModel (rm);
		} else if (q.isDescribeType()) {
			Model rm = qe.execDescribe();
			setLastResultModel (rm);
		} else if (q.isAskType() ) {
			boolean rf = qe.execAsk();
			setLastResultFlag(rf);
		}
	}
	synchronized void execCleanup() throws Throwable {
		if (myQueryExecution != null) {
			myQueryExecution.close();
			myQueryExecution = null;
		}
	}
	
	public synchronized void exec() throws Throwable {
		try {
			setUp();
			execPrepare();
			execQuery();
			tearDown();
		} finally {
			execCleanup();
		}
	}
	
	public synchronized void runAndDump () {
		try {
			theLog.info("=========================================================\n"
					+   "runAndDump(" + myManifestEntryURI + ")");
			exec();
			String xmlOut = getLastResultXML();
			theLog.info("XML Out\n=========================================================\n" 
		        + xmlOut + "\n==========================================================");
		} catch (Throwable t) {
			theLog.error("runAndDump", t);
		}
	}
	
	public TestCase makeJUnitTestCase () {
		return new PeruserARQTestCase(this);
	}
	public String getTestName () {
		return myTestName;
	}
    private static String fixName(String s) {
        s = s.replace('(','[') ;
        s = s.replace(')',']') ;
        return s ;
    }
    protected void setUp() throws Exception   {
        // SPARQL and ARQ tests are done with no value matching (for query execution and results testing)
        if ( ! my_isRDQLtestFlag )
        {
            myResetNeededFlag = true ;
           // ARQ.getContext().setTrue(ARQ.graphNoSameValueAs) ;
		   ARQ.getContext().setTrue(ARQ.strictGraph) ;
        }
		boolean tihd = SPARQL_Utils.doesTestItemHaveDataset(myARQTestItem);
		if (tihd) {
			if (myInputDataset == null) {
				myInputDataset = createDataset(myARQTestItem.getDefaultGraphURIs(), myARQTestItem.getNamedGraphURIs()) ;
			} else {
				throw new Exception ("TestItem dataset supplied, but I already have a dataset!");
			}
		}
        myExpectedResultsModel = constructResultsModel(myARQTestItem.getResultFile());
    }
    
    protected void tearDown() throws Exception  {
        if (myResetNeededFlag) {
            ARQ.getContext().setFalse(ARQ.strictGraph) ;
		}
    }
    
    private Model constructResultsModel(String filename) {
        if ( filename == null ) {
            return null ;
		}
        // Knows about RDF and XML forms. 
        return ResultSetFactory.loadAsModel(filename) ;
    }
	
	protected void parseRulesFile() throws Throwable {
		String rulesFileURL = myPeruserTestItem.getRulesFileName();
		theLog.info("rulesFileURL=" + rulesFileURL);
		if (rulesFileURL != null) {
			myReasoner = ReasonerUtils.buildReasonerFromRulesAtURL(rulesFileURL);
        }
	}	

    private static Dataset createDataset(List defaultGraphURIs, List namedGraphURIs)    {
        return DatasetUtils.createDataset(defaultGraphURIs, namedGraphURIs, null, null) ;
    }
	protected String checkDataset() {
		String 	errorFound = null;
		boolean qhd = SPARQL_Utils.doesQueryHaveDataset(myParsedQuery);
		boolean tihd = (myInputDataset != null);
		boolean tihr = (myExpectedResultsModel != null); 
		 
		if (qhd && tihd && tihr )    {
			// Syntax tests may have FROM etc and a manifest data file, so only warn if there are results to test. 
			theLog.warn(myARQTestItem.getName()+" : is results-testable, AND has BOTH query data source AND file test source") ;
		} else if (!qhd && !tihd) {
			errorFound = "No dataset supplied by query, input file, or constructor!";
		}
		return errorFound;
	}
	
	public Query getParsedQuery () {
		return myParsedQuery;
	}
	
	public QueryExecution makeQueryExecution() throws Throwable {
		Query q = myParsedQuery;
		QueryExecution qe = null;
		// This was in HP's original QueryTest, but appears to be a bug:
		// QueryExecutionFactory.create(q, myQueryFileManager) ;
		if (myInputDataset == null) {
			qe = QueryExecutionFactory.create(q) ;
		} else {
			theLog.info("Constructing the QueryExecution with an explicit dataset");
			
			Dataset inferredDataset = myInputDataset;
			if (myReasoner != null) {
				theLog.info("Wrapping the explicit dataset with inference based on explicit rules");
				inferredDataset = ReasonerUtils.makeInferredDataset (myInputDataset, myReasoner);
			}
			
			 qe = QueryExecutionFactory.create(q, inferredDataset);
			// qe = QueryExecutionFactory.create(q, myInputDataset) ;
		} 
		if (myQueryFileManager != null ) {
			qe.setFileManager(myQueryFileManager);
		}
		// Here's a fancy QueryExecution feature not used in the original HP test
		// qe.setInitialBinding(QuerySolution binding) 
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		qe.setInitialBinding(initialBinding);
		return qe;
	}
	
	public boolean checkSelectResult (ResultSetRewindable actualRSR) throws Exception {		
		boolean result = true;
		if (myExpectedResultsModel != null) {
			// Is this necessary only to satisfy the debugging code?
			ResultSetRewindable expectedRSR = ResultSetFactory.makeRewindable(myExpectedResultsModel) ;
			result = SPARQL_Utils.resultSetEquivalent(actualRSR, expectedRSR);
		}
		return result;
    }
   
    public String checkModelResult (Model resultsActual, String queryFlavorDebug) throws Exception {
		String failureDescription = null;
        if (myExpectedResultsModel != null) {
            try {
                if (!myExpectedResultsModel.isIsomorphicWith(resultsActual))   {
                    printFailedModelTest(resultsActual, myExpectedResultsModel) ;
                    failureDescription = "Results of " + queryFlavorDebug + " query do not match for: "+ myARQTestItem.getName();
                }
            } catch (Exception ex)  {
				String warnString = "Caught exception in " + queryFlavorDebug + " model result testing - ";
                theLog.warn(warnString, ex) ;
                failureDescription = warnString + ex;
            }
        } else {
			theLog.warn("No expected results to compare with actual");
		}
		return failureDescription;
	}
	public String checkAskResult(boolean actualResultFlag) {
		String failureDescription = null;
        if ( myExpectedResultsModel != null ) {
			// Go to incredible pains to determine whether we are intended to see true or false
            StmtIterator sIter = myExpectedResultsModel.listStatements(null, RDF.type, 
							com.hp.hpl.jena.vocabulary.ResultSet.ResultSet) ;
            if ( !sIter.hasNext()) {
                throw new QueryTestException("Can't find the ASK result") ;
			}
            Statement s = sIter.nextStatement() ;
            if (sIter.hasNext()) {
                throw new QueryTestException("Too many result sets in ASK result") ;
			}
            Resource r = s.getSubject() ;
            Property p = myExpectedResultsModel.createProperty(
				com.hp.hpl.jena.vocabulary.ResultSet.getURI()+"boolean") ;
            
            boolean expectedResultFlag = r.getRequiredProperty(p).getBoolean();
            if (expectedResultFlag != actualResultFlag ) {
                failureDescription = "Actual ASK test result [" + actualResultFlag + "] does not match expected[" + expectedResultFlag + "]";
			}
        }
		return failureDescription;
    }
    
    void printFailedResultSetTest(ResultSetRewindable qr1)
   {
	   ResultSetRewindable qr2 = ResultSetFactory.makeRewindable(myExpectedResultsModel) ;
       PrintStream out = System.out ;
       out.println() ;
       out.println("=======================================") ;
       out.println("Failure: "+description()) ;
       
       out.println("Got: "+qr1.size()+" --------------------------------") ;
       qr1.reset() ;
       ResultSetFormatter.out(out, qr1, myParsedQuery.getPrefixMapping()) ;
       qr1.reset() ;
       
       if (thePrintModelsOnFailureFlag) {
           out.println("-----------------------------------------") ;
           SPARQL_Utils.resultSetToModel(qr1).write(out, "N3") ;
           qr1.reset() ;
       }
       out.flush() ;
       
       out.println("Expected: "+qr2.size()+" -----------------------------") ;
       qr2.reset() ;
       ResultSetFormatter.out(out, qr2, myParsedQuery.getPrefixMapping()) ;
       qr2.reset() ;
       
       if (thePrintModelsOnFailureFlag)  {
           out.println("---------------------------------------") ;
           SPARQL_Utils.resultSetToModel(qr2).write(out, "N3") ;
           qr2.reset() ;
       }
       out.println() ;
       out.flush() ;
   }

   void printFailedModelTest(Model results, Model expected) {
        PrintWriter out = FileUtils.asPrintWriterUTF8(System.out) ;
        out.println("=======================================") ;
        out.println("Failure: "+description()) ;
        if (thePrintModelsOnFailureFlag) {
            results.write(out, "N3") ;
            out.println("---------------------------------------") ;
            expected.write(out, "N3") ;
        }
        out.println() ;
    }
    
    public String toString() {
		return description();
	}
    private String description() {
		return myTestName;    
    }
}

/**
 *  Note:  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  and is redistributed in MODIFIED form according to the terms of the following 
 *  license. 
 */
 
 /*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
