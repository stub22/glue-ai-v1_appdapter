/*
 */

package com.appdapter.binding.dom4j;

import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu Baurmann
 */
public class D4J_XPath {
	static Logger theLogger = LoggerFactory.getLogger(D4J_XPath.class);
	public static Integer xpathIntValue(Element dom4jElement, String xpathExpr) {
		Integer result = null;
		try {
			String stringVal = dom4jElement.valueOf(xpathExpr);
			theLogger.info("xpathIntValue[" + xpathExpr + "] found value: " + stringVal);
			if ((stringVal != null) && (stringVal.length() > 0)) {
				result = Integer.parseInt(stringVal);
			}
		} catch (Throwable t) { }
		return result;
	}
	public static String xpathStringValue(Element dom4jElement, String xpathExpr) {
		String result = null;
		try {
			String stringVal = dom4jElement.valueOf(xpathExpr);
			if ((stringVal != null) && (stringVal.length() > 0)) {
				result = stringVal;
			}
		} catch (Throwable t) { }
		return result;
	}
	public static Integer getOptionalIntegerValueAtXPath(Node dom4JDoc, String xpath) throws Throwable {
		Integer result = null;
		String val = dom4JDoc.valueOf(xpath);
		if ((val != null) && (val.length() > 0)) {
			result = Integer.parseInt(val);
		}
		return result;
	}
	
}
