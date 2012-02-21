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

package org.appdapter.peru.core.document;

import java.io.File;

import java.net.URL;

import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

import com.hp.hpl.jena.rdf.model.Model;

import org.appdapter.peru.binding.jena.ModelUtils;

import org.appdapter.peru.core.operation.Operation;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/** 
 *  This command runs an XSLT transform from the input doc to the output doc.
 * <BR/>
 * Currently we are dumping the output doc to stdout, and then throwing it away.
 * Also, we are not using the input model or even the input params.
 * <BR/>
 * The only thing we are doing right, in fact, is calling ModelUtils.getResourceURL.
 * And, it looks like the XSLT wrapper code is OK, too.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class TransformCommand implements Operation {
    static OutputFormat PRETTY_PRINT_OUTPUT_FORMAT = OutputFormat.createPrettyPrint();
	
	public Model execute(Individual op, OntModel input, Model target, Map params) throws Throwable {
		System.out.println("##########################################SubstrateTransformOperation is chewin cycles, HONEY!");
		
		File f = new File("local.txt");
		String lp = f.getAbsolutePath();
		System.out.println("Example local path converted to absolute: " + lp);

		
		String xmlInputPath = "substrate/xhtml/ts_out_002.xhtml";
		String xsltPath = "substrate/xslt/bookmarks2rdf.xslt";
		
		URL xmlInputURL = ModelUtils.getResourceURL(xmlInputPath, getClass());
		URL xsltURL = ModelUtils.getResourceURL(xsltPath, getClass());
		
		run(xmlInputURL.toString(), xsltURL.toString());
		
		// run("file:substrate/xhtml/ts_out_002.xhtml", "file:substrate/xslt/bookmarks2rdf.xslt");
		
		// run("file:substrate/xhtml/ts_out_002.xhtml", "file:substrate/xslt/bookmarks2rdf.xslt");
		// run("/app/japp/substrate/pdat/transforms/ts_out_002.xhtml", "/app/japp/substrate/pdat/transforms/bookmarks2rdf.xslt");

		return null;
	}
	
	public static Document execTransform (String docUrl, String xfmUrl) {
		Document transformedDoc = null; 
		try {
			SAXReader reader = new SAXReader();
			Document inDoc = reader.read(docUrl);
         
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(xfmUrl));
        
			// now lets create the TrAX source and result
			// objects and do the transformation
			Source inSrc = new DocumentSource( inDoc );   
			
			DocumentResult result = new DocumentResult();
			transformer.transform( inSrc, result );
	
			// output the transformed document
			 transformedDoc = result.getDocument();
			
		} catch (DocumentException e) {
            System.out.println( "Exception occurred: " + e );
            Throwable nestedException = e.getNestedException();
            if ( nestedException != null ) {
                System.out.println( "NestedException: " + nestedException );
                nestedException.printStackTrace();
            }
            else {
                e.printStackTrace();
            }
        } catch (Throwable t) {
            System.out.println( "Exception occurred: " + t );
            t.printStackTrace();
        }
		return transformedDoc;
	}
	public static void run (String docUrl, String xfmUrl) {
        try {
			// System.out.println(transformedDoc.asXML());
			
			Document result = execTransform (docUrl, xfmUrl);
			
			XMLWriter writer = new XMLWriter( System.out, PRETTY_PRINT_OUTPUT_FORMAT);
			writer.write( result );
			writer.flush();
        }
        catch (Throwable t) {
            System.out.println( "Exception occurred: " + t );
            t.printStackTrace();
        }
    }

}
