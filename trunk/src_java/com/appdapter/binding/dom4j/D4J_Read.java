/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.binding.dom4j;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

/**
 *
 * @author Stu Baurmann
 */
public class D4J_Read {

	public static Document wrapAndParseXmlText(String xmlText, String wrapperTagName) throws Throwable {
		String rooted = "<" + wrapperTagName + ">" + xmlText + "</" + wrapperTagName + ">";
		Document rootedDoc4J = DocumentHelper.parseText(rooted);
		return rootedDoc4J;
	}


}
