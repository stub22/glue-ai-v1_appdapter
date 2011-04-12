package net.peruser.module.selector;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


import net.peruser.core.name.Address;

import net.peruser.core.name.CoreAddress;

import net.peruser.binding.jena.SPARQL_Utils;

import net.peruser.core.config.Config;

import net.peruser.core.document.Doc;

import net.peruser.binding.dom4j.Dom4jDoc;


import net.peruser.core.environment.Environment;

import net.peruser.core.machine.DocProcessorMachine;

import org.apache.avalon.framework.parameters.Parameters;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static net.peruser.module.selector.SelectorAddressConstants.*;


/**  This class implements a "simple" form of SPARQL query.  <br/>
 * <ol>
 *		<li>The query text  from a </li>
 * </ol>
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class SimpleQueryMachine extends DocProcessorMachine {
	private static Log 		theLog = LogFactory.getLog(SimpleQueryMachine.class);

	/** Provides a simplified pathway for configuration, suitable in unit testing situations.
	 *  <br/>Not used in cocoon-deployed situations.
	 * 
	 */
	public void setup(String configPath, Environment env) throws Throwable {
		 super.setup(configPath, env);

	}
	/**
	 * In a cocoon environment, the input doc will already have been 
	 */
		
	protected  Doc processDoc(Address instructAddr, Doc input) throws Throwable {
		Doc resDoc = null;

		Environment env = getCurrentEnvironment();		

		Config curConfig = getCurrentConfig();

/* Note that in a CocoonConsolidatedConfig constructed by PeruserCocoonKernel, the sitemap parameters are to be found at instructAddr
 */
 
 		theLog.info("***************SimpleQueryMachine.processDoc() got instructAddress: " + instructAddr);
		 
		Address qfa = new CoreAddress("peruser_uri_scheme:queryFile");
		Address dfia = new CoreAddress("peruser_uri_scheme:dataFile");
		Address dfoa = new CoreAddress("peruser_uri_scheme:dataFormat");
		Address dbua = new CoreAddress("peruser_uri_scheme:dataBaseURI");

		String conf_queryFile = curConfig.getSingleString(instructAddr, qfa);
		 
		String conf_dataFile = curConfig.getSingleString(instructAddr, dfia);
		 
		String conf_dataFormat  = curConfig.getSingleString(instructAddr, dfoa);
		 
		String conf_dataBaseURI  = curConfig.getSingleString(instructAddr, dbua);
		 
		theLog.info("***************SimpleQueryMachine.processDoc() found queryFile param: " + conf_queryFile);

		String modelURL = env.resolveFilePath(conf_dataFile);
		String queryFileURL = env.resolveFilePath(conf_queryFile);
		String modelBaseURI = conf_dataBaseURI;
		String modelFormat = conf_dataFormat;
		
		// First, read whatever RDF data is in the input
		Model inputModel = produceModelFromInstructions(input);
		// Now add data from the model referred to by our sitemap params
		FileInputStream	modelInputStream = new FileInputStream(modelURL);
		inputModel.read(modelInputStream, modelBaseURI, modelFormat);
		
		String resultXML = SPARQL_Utils.executeQueryFromFile (queryFileURL, inputModel);
		// System.out.println("resultXML=" + resultXML);
		Document resultDoc4J = DocumentHelper.parseText(resultXML);
		
		resDoc = new Dom4jDoc (resultDoc4J);

		return resDoc;
	}
	/**
	 *  Looks in the instruction doc for embedded RDF/XML models matching the XPath address //pmd:model_set/pmd:model, where
	 *  pmd = http://www.peruser.net/model_description.  All such contents are collected into a single jena "Model" and returned. 
	 */
			
	protected static Model produceModelFromInstructions(Doc instructionDoc) throws Throwable {
		Model instructiveModel = ModelFactory.createDefaultModel();
		Document	d4jDoc = ((Dom4jDoc) instructionDoc).getDom4jDoc();
		Map namespaceMap = new HashMap();
		namespaceMap.put("pmd", "http://www.peruser.net/model_description");
		
		XPath modelDescXPath = d4jDoc.createXPath("//pmd:model_set/pmd:model");
		modelDescXPath.setNamespaceURIs(namespaceMap);
		List results = modelDescXPath.selectNodes(d4jDoc);
		for (Iterator iter = results.iterator(); iter.hasNext(); ) {
			Element modelE = (Element) iter.next();
			String format = modelE.valueOf("@format");
			theLog.info("Found model of format: " + format);
			if (format.equals("RDF/XML")) {
				Element modelRootElement = modelE.element("RDF");
				String modelXML = modelRootElement.asXML();
				theLog.debug("Model dump:\n===========================\n " + modelXML + "\n============================");
				StringReader	mxsr = new StringReader(modelXML);
				String modelBaseURI = null;
				instructiveModel.read(mxsr, modelBaseURI, format);
			}
		}
		return instructiveModel;
	}

		
}		
