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

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.core.DataFormat; 	
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import com.hp.hpl.jena.util.junit.Manifest;
import com.hp.hpl.jena.util.junit.ManifestItemHandler;
import com.hp.hpl.jena.util.junit.TestUtils;
import com.hp.hpl.jena.sparql.junit.TestItem;


import com.hp.hpl.jena.sparql.vocabulary.TestManifest;
import com.hp.hpl.jena.sparql.vocabulary.VocabTestQuery;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This item extends the (Jena+ARQ) TestItem, and adds handling for special Peruser properties to the jena-ARQ-test-manifest properties
 *
 * 2010 Update - ARQ TestItem now has private constructors, so this class needs to be reworked.  Hacked to compile+run in 2010-Oct.
 *
 * @author      Stu B. <www.texpedient.com> 
 * @version     @PERUSER_VERSION@
 */
public class PeruserARQTestItem  {
	private static Log 				theLog = LogFactory.getLog(PeruserARQTestItem.class);
	
	private String myRulesFileName = null;
	private	TestItem		myARQTestItem;
	// This is close to the style used in the jena vocabulary packages, which are different in ARQ and Jena:
	
	//  For names  in the "mf:" space, we use this (but also see TestManifestX!)
	// public static final Property 	rulesPROP = ResourceFactory.createProperty(TestManifest.getURI() + "rules" );
	//  For names  in the "qt:" space, we use this:
	public static final Property 	rulesPROP = ResourceFactory.createProperty(VocabTestQuery.getURI() + "rules" );
	
	public PeruserARQTestItem(Resource r, Resource defaultTestType, Syntax defaultQuerySyntax, DataFormat defaultDataSyntax)
    {
		myARQTestItem = TestItem.create( r,  defaultTestType,  defaultQuerySyntax,  defaultDataSyntax);
		_extractRulesFileName();
		theLog.debug("PeruserARQTestItem constructed using abstract objs, myRulesFileName=" + myRulesFileName);
	}

	public PeruserARQTestItem(String _name, String _queryFile, String _dataFile, String _resultFile, String _rulesFile) {
		myARQTestItem = TestItem.create(_name, _queryFile, _dataFile, _resultFile);
		myRulesFileName = _rulesFile;
		theLog.debug("PeruserARQTestItem constructed using strings, myRulesFileName=" + myRulesFileName);
	}
	public TestItem getARQTestItem() {
		return myARQTestItem;
	}
	public String getRulesFileName () {
		return myRulesFileName;
	}
	/**  The location of the sequence of inference modules should be the result of a query.
	 */
	protected String _extractRulesFileName() {
		if (myRulesFileName == null) {
			Resource tares = getActionResource();
			if (tares != null) {
				theLog.debug("rulesPROP = " + rulesPROP);
				myRulesFileName =  TestUtils.getLiteralOrURI(tares, rulesPROP);
			}
		}
		return myRulesFileName;
	}
	
	protected Resource getActionResource() {
		Resource testRes = myARQTestItem.getResource();
		Resource testAction = testRes.getProperty(TestManifest.action).getResource();
		return testAction;
	}
	
	public List getInferenceSpecs() {
		List	inferSpecs = new ArrayList();
		return inferSpecs;
	}
	
	public Query parseQueryFile() throws Throwable {
		Query parsedQuery = null;
   		String queryFileURL = myARQTestItem.getQueryFile();
		String queryBaseURI = null;
		Syntax queryFileSyntax = myARQTestItem.getQueryFileSyntax();
		theLog.info("queryFileURL=" + queryFileURL + "  queryBaseURI=" + queryBaseURI + "  queryFileSyntax=" + queryFileSyntax);
		if (queryFileURL == null) {
			throw new Exception("Query file URL is null");
        }
        parsedQuery = QueryFactory.read(queryFileURL, queryBaseURI, queryFileSyntax);
		return parsedQuery;
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


