package net.peruser.module.selector;

import java.io.Reader;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.DataSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.peruser.binding.jena.ModelUtils;
import net.peruser.binding.jena.ReasonerUtils;
import net.peruser.binding.jena.SPARQL_Utils;

import net.peruser.core.command.DocCommand;

import net.peruser.core.config.Config;

import net.peruser.core.document.Doc;
import net.peruser.binding.dom4j.Dom4jDoc;

import net.peruser.core.environment.Environment;

import net.peruser.core.name.Address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import static net.peruser.module.selector.SelectorAddressConstants.*;


/**  SelectorCommand implements SPARQL (and eventually other, similar) query mechanisms for peruser Machines.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class SelectorCommand extends DocCommand  {
	private static Log 		theLog = LogFactory.getLog(SelectorCommand.class);

	private	DataSource			myDataset;
	private	String			myQueryFilePath;

	public Doc workDoc(Doc instructionDoc) throws Throwable {		
		augmentDataSourceFromInstructions(myDataset, instructionDoc);
		
		SPARQL_Utils.dumpDatasetNames(myDataset);

		String resultXML = SPARQL_Utils.runQueryOverDataset (myQueryFilePath, myDataset);
		Document resultDoc4J = DocumentHelper.parseText(resultXML);
		// theLog.debug("workDoc() resDoc=" + resultDoc4J.asXML());
		Doc resDoc = new Dom4jDoc (resultDoc4J);
		return resDoc;
	}

	public void configure (Environment env, Config configImpl, Address configInstanceAddress)
				throws Throwable {
		theLog.debug("SelectorCommand.configure() - START");
		super.configure(env, configImpl, configInstanceAddress);
		Config opConf = configImpl;

		Address commandConfig = configInstanceAddress;
		
		Address queryPlace =  opConf.getSingleAddress(commandConfig, queryFilePlacePropAddress);
		String queryFilePath = getMappedPlaceURL(queryPlace);
		myQueryFilePath = queryFilePath;
		
		// if (commandConfig  isA DirectModelSelectorCommandConfig
		/* DatasetFactory      public static Dataset make(Dataset ds,
                           com.hp.hpl.jena.rdf.model.Model defaultModel)
public DataSet							   
		*/
		
		DataSource datasrc = DatasetFactory.create();
		
		List inputModelConfigs = opConf.getFieldValues(commandConfig, inputModelPropAddress);
		Iterator imcit = inputModelConfigs.iterator(); 
		while (imcit.hasNext()) {
			Address inputModelConfig = (Address) imcit.next();
			Address inferenceConfig = opConf.getOptionalAddress(inputModelConfig, inferenceConfigPropAddress);
			
			OntModelSpec ontModelSpec = ReasonerUtils.lookupOntModelSpec(opConf, inferenceConfig);
			
			Address modelPlace = opConf.getSingleAddress(inputModelConfig, modelPlacePropAddress);
			
			String modelFilePath = getMappedPlaceURL(modelPlace);
			
			Model baseModel = ModelUtils.loadJenaModelUsingJenaFileManager(myEnvironment, modelFilePath);
			Model queryModel = baseModel;
			if (ontModelSpec != null) {
				OntModel ontModel = ModelFactory.createOntologyModel(ontModelSpec, baseModel);
				queryModel = ontModel;
			}
			// This is the URI that must be matched to the GRAPH name in the SPARQL query
			// If it's absent, then we're dumping the model into the "default graph".
			String inputNameURI = null;
			Address inra =  opConf.getOptionalAddress(inputModelConfig, inputNameRefPropAddress);
			if (inra != null) {
				Address inputNameResolvedAddress = resolveRef(inra);
				inputNameURI = inputNameResolvedAddress.getResolvedPath();
			}

			SPARQL_Utils.mergeModelIntoDataSource(datasrc, inputNameURI, queryModel);
		}
		SPARQL_Utils.ensureDefaultModelNotNull(datasrc);

		 
		
		myDataset = datasrc;
		theLog.debug("SelectorCommand.configure() - END");
	}

	
	protected static void augmentDataSourceFromInstructions(DataSource ds, Doc instructionDoc) throws Throwable {
		Document	d4jDoc = ((Dom4jDoc) instructionDoc).getDom4jDoc();
		Map namespaceMap = new HashMap();
		namespaceMap.put("pmd", "http://www.peruser.net/model_description");
		XPath modelDescXPath = d4jDoc.createXPath("//pmd:model_set/pmd:model");
		modelDescXPath.setNamespaceURIs(namespaceMap);
		List results = modelDescXPath.selectNodes(d4jDoc);
		for (Iterator iter = results.iterator(); iter.hasNext(); ) {
			Element modelE = (Element) iter.next();
			String format = modelE.valueOf("@format");
			String inputName = modelE.valueOf("@name");
			theLog.debug("Found model with name " + inputName + " and format " + format);
			if (format.equals("RDF/XML")) {
				Element modelRootElement = modelE.element("RDF");
				String modelXML = modelRootElement.asXML();
				theLog.debug("Model text dump:\n===========================\n " + modelXML + "\n============================");
				StringReader	mxsr = new StringReader(modelXML);
				String modelBaseURI = null;
				Model instructiveModel = ModelFactory.createDefaultModel();
				instructiveModel.read(mxsr, modelBaseURI, format);
				SPARQL_Utils.mergeModelIntoDataSource(ds, inputName, instructiveModel);
			}
		}
	}
}
