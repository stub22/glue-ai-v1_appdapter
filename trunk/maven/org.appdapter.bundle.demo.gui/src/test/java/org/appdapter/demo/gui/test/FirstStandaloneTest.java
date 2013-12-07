package org.appdapter.demo.gui.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class FirstStandaloneTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public FirstStandaloneTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(FirstStandaloneTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		//ext.bundle.openconverters.osgi.Activator.this.ensureConvertersClassesAreFindable();
		assertTrue(true);
	}
}
