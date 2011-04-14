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

package com.appdapter.binding.dom4j;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class D4J_Write {
	public static org.w3c.dom.Node write(Document d4jDoc) throws Throwable {
		DOMWriter domWriter = new DOMWriter();
		org.w3c.dom.Document w3cDoc = domWriter.write(d4jDoc);
		org.w3c.dom.Element w3cEl = w3cDoc.getDocumentElement();
		return w3cEl;
	}
	public static void writePretty (Node d4jNode, OutputStream outStream) throws Throwable {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(outStream, format);
		writer.write(d4jNode);
	}
	public static void writePretty (Node d4jNode, Writer writer) throws Throwable {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter xwriter = new XMLWriter(writer, format);
		xwriter.write(d4jNode);
	}
	public static String writePrettyString (Node d4jNode) throws Throwable {
		StringWriter	sw = new StringWriter();
		writePretty(d4jNode, sw);
		String result = sw.toString();
		return result;
	}

}
