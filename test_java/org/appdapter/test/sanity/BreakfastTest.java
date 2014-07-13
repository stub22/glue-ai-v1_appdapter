/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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
package org.appdapter.test.sanity;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import junit.runner.Version;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class BreakfastTest {
	
	public BreakfastTest() {
		System.out.println("Behold the morning - a BreakfastTest is constructed!");
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
			System.out.println("Yes, let's setUpClass");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}
	
	@Before
	public void setUp() {
		System.out.println("setUp is happenin");
	}
	
	@After
	public void tearDown() {
		System.out.println("dun got tore down");
	}

	@Test
	public void boilSomeWater() {
		System.out.println("Yea, I smell the water boiling");
	}
	@Test
	public void scrambleTheEggs() {
		System.out.println("scram-scram delish");
		        Collection collection = new ArrayList();
        assertTrue(collection.isEmpty());
	}
	@Ignore("No time for toast test today, ma!")
	@Test
	public void toastTheBagel() {
		System.out.println("The bagel MUST be toasty!");
	}
	
	/*
	 * If you are running your JUnit 4 tests with a JUnit 3.x runner, write a suite() method that uses the JUnit4TestAdapter class to create a suite containing all of your test methods:

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SimpleTest.class);
    }
	 */
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(BreakfastTest.class);
    }	
	/*
	 * Still getting:
	 * [surefire:test]
Surefire report directory: E:\_data\_mount\appdapter_trunk\maven\org.appdapter.lib.core\target\surefire-reports
Behold the morning - a BreakfastTest is constructed!

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.appdapter.test.sanity.BreakfastTest
Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 sec

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
	 * 
	 * 
	 * http://stackoverflow.com/questions/2332832/no-tests-found-with-test-runner-junit-4 
	 * I solved it by renaming one of the test methods to start with "test..." (JUnit3 style) 
	 * and then all tests are found. I renamed it back to what it was previously, and it still works.
	 */
	public void testJunit473IsJunk() { 
		System.out.println("Testing that junit4.7.3 is a pile of poo");
		System.out.println("Actual JUnit version = " + Version.id());
	}
}
