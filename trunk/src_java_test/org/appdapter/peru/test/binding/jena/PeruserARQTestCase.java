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

package org.appdapter.peru.test.binding.jena;

import java.io.IOException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

import com.hp.hpl.jena.query.ResultSetRewindable;

import com.hp.hpl.jena.rdf.model.Model;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * JUnit-Test-Suite for ARQ (+inference), based on the Jena Junit/query code.
 *
 * @author      Stu B. <www.texpedient.com>, based on code by Andy Seaborne of HP.
 * @version     @PERUSER_VERSION@
 * @copyright   derivative(HP, Appdapter)
 */
 
public class PeruserARQTestCase extends TestCase {
    private static Log 		theLog = LogFactory.getLog( PeruserARQTestCase.class );
    
    private static int 		theTestCounter = 1 ;

    // -- Items from construction
	private		int 		myTestNumber = theTestCounter++ ;

	private		PeruserARQ_Harness		myHarness;
	
	public PeruserARQTestCase() {
		// For use by Naive JUnit
	} 
	
    PeruserARQTestCase(PeruserARQ_Harness harness) {
		super(harness.getTestName());
		myHarness = harness;
    }
    protected void setUp() throws Exception   {
        super.setUp();
		myHarness.setUp();
    }
    
    protected void tearDown() throws Exception  {
		myHarness.tearDown();
        super.tearDown() ;
    }
	protected void verifySingleDataset() {
		String errorFound = myHarness.checkDataset();
		if (errorFound != null) {
			fail(errorFound);
		}
	}
    protected void runTest() throws Throwable {
		if ((myTestNumber % 15) == 0) {
			fail("Every 15th test fails!");
		}
		try {
			myHarness.execPrepare();
			verifySingleDataset();                      
			myHarness.execQuery();
			Query q = myHarness.getParsedQuery();
			if (q.isSelectType() ) {
				ResultSet resultsActual = myHarness.getLastResultSet();
				ResultSetRewindable actualRSR = ResultSetFactory.makeRewindable(resultsActual) ;
				verifySelectResult(actualRSR);
			} else if (q.isConstructType() ) {
				Model resultsActual = myHarness.getLastResultModel();
				verifyModelResult(resultsActual, "'CONSTRUCT'");
			} else if (q.isDescribeType() ) {
				Model resultsActual = myHarness.getLastResultModel();
				verifyModelResult(resultsActual, "'DESCRIBE'");
			} else if (q.isAskType() ) {
				boolean actualResultFlag = myHarness.getLastResultFlag().booleanValue();
				verifyAskResult(actualResultFlag);            
			}
        } catch (IOException ioEx) {
            theLog.error("IOException: ",ioEx) ;
            fail("IOException: "+ioEx.getMessage()) ;
            throw ioEx ;
        }  catch (NullPointerException ex) { throw ex ; }
        catch (Exception ex) {
            theLog.error("Exception: "+ex.getMessage(),ex) ;
            fail( "Exception: "+ex.getClass().getName()+": "+ex.getMessage()) ;
        }
		finally {
			myHarness.execCleanup();
		}
    }
 	
	private void verifySelectResult (ResultSetRewindable actualRSR) {		
        try {
			boolean b = myHarness.checkSelectResult(actualRSR); 
			if (!b) {
				myHarness.printFailedResultSetTest(actualRSR);
			}
			assertTrue("Actual results do not match expected for: " + myHarness.getTestName(), b) ;
		} catch (	Exception ex) {
			theLog.warn("runTestSelect - Exception in result testing", ex) ;
			fail("runTestSelect - Exception in result testing: " + ex) ;
		}
	}
    
    private void verifyModelResult (Model resultsActual, String queryFlavorDebug) throws Exception {
		String failureDescription = myHarness.checkModelResult(resultsActual, queryFlavorDebug);
		if (failureDescription != null) {
			fail(failureDescription);
        } 
	}
	private void verifyAskResult(boolean actualResultFlag) {
		String failureDescription = myHarness.checkAskResult(actualResultFlag);
		if (failureDescription != null) {
			fail(failureDescription);
        }
    }
    
    public String toString() { 
		String mtin = myHarness.getTestName();
        if (mtin != null ) {
            return mtin;
		} else {
			return super.getName();
		}
    }

    // Cache
    String _description = null ;
    private String description() {
        if ( _description == null )
            _description = makeDescription() ;
        return _description ;
    }
    
    private String makeDescription() {
        String d = "Test "+myTestNumber+" :: "+myHarness.getTestName() ;
        return d ;
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

