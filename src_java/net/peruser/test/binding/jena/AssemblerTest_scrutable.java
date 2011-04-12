package net.peruser.test.binding.jena;

import net.peruser.binding.jena.SPARQL_Utils;

import java.io.Reader;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.assembler.Mode;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;

import com.hp.hpl.jena.sparql.util.TypeNotUniqueException;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.DataSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.peruser.binding.jena.JenaKernel;
import net.peruser.binding.jena.ModelUtils;
import net.peruser.binding.jena.ReasonerUtils;
import net.peruser.binding.jena.SPARQL_Utils;
import net.peruser.binding.jena.AssemblerUtils;

import net.peruser.core.command.AbstractCommand;

import net.peruser.core.document.Doc;

import net.peruser.core.environment.Environment;
import net.peruser.binding.console.ConsoleEnvironment;
import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;
import net.peruser.core.name.Abbreviator;
import net.peruser.core.name.CoreAbbreviator;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import org.dom4j.io.SAXReader;

import static net.peruser.test.data.TestDataConstants_scrutable.AssemblerUnitTestConstants.*;

/**  We need to test the heck out of the FileManager, Assember, and possibly OntDocumentManager,
 *   until all is completely understood..
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class AssemblerTest_scrutable {
	private static Log 		theLog = LogFactory.getLog(AssemblerTest_scrutable.class);


	
	public static void main(String[] args) {
		System.out.println("AssemblerTest - inflating!");		
		String 	bootDataURL = null;
		
		Model targetModel = null;
		
        try {
			if (args.length >= 1) {
				bootDataURL = args[0]; 
			}	
            Model assemblerSpecModel = null ;
			
			Environment consoleTestEnv = new ConsoleEnvironment();		
			
			// Relying on boot information is always a fallback position
			
			String kernelCuteName = "assembler_test_kernel";
			String kernelPubName = "jena_kernel:assembler_test_kernel_at_" + bootDataURL;
				
			Address kernelAddress = new CoreAddress(kernelPubName);
			
			JenaKernel 	jenaKernel = new JenaKernel(consoleTestEnv, bootDataURL, kernelCuteName, kernelAddress);
			jenaKernel.dumpDebug();
			// String queryResultXML = runQueryOverDataset (jenaKernel, "my_cute_combo_dataset", AT_cameraComboDatasetURI, AT_cameraQueryLoc);
			
			String datasetCuteName = "my_cute_census_dataset";
			String namespaceURI = "http://www.peruser.net/2008/census#";
			String datasetURI = namespaceURI + "census_combo_dataset_001";
			String queryLoc = "file:app/census/sparql/census_query_001.sparql";
			String queryResultXML = runQueryOverDataset (jenaKernel, datasetCuteName, datasetURI, queryLoc);
						
			// Model keysModel = AssemblerUtils.getAssembledModel(jenaKernel, AT_cameraKeysModelURI);
			
			theLog.debug("Query Result XML: " + queryResultXML);
			
			jenaKernel.dumpDebug();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("AssemblerTest - deflating!");
	}
	public static String runQueryOverDataset(JenaKernel jenaKernel, String datasetCuteName, String datasetAssemblyURI, String queryPath)
				throws Throwable {
		Dataset	originalDataset = jenaKernel.assembleAndAttachDataset(datasetCuteName, datasetAssemblyURI);
		DataSource comboDataset = SPARQL_Utils.makeIndependentDataSourceFromDataset(originalDataset);
		
		theLog.debug("Combo Dataset: " + comboDataset);
		Iterator dnit = comboDataset.listNames();
		while (dnit.hasNext()) {
			String	modelName = (String) dnit.next();
			theLog.debug("found model name: " + modelName);
		}
		theLog.debug("Query URL: " + queryPath);
		
		String queryResultXML = SPARQL_Utils.runQueryOverDataset(queryPath, comboDataset);		
		
		return queryResultXML;
	}
}					


			/*
			Model testModel = AssemblerUtils.getAssembledModel(jenaKernel, assemblerResURI);
			theLog.debug("testModel=" + testModel);
			Model comboModel = AssemblerUtils.getAssembledModel(jenaKernel, assemblerResURI);
			*/
			// pickyComboAs
						
/*
			SAXReader reader = new SAXReader();
			Document deltaDoc = reader.read(AT_deltaDocPath);
			
			Map namespaceMap = new HashMap();
			namespaceMap.put("pmd", "http://www.peruser.net/model_description");
			XPath modelDescXPath = deltaDoc.createXPath("//pmd:model_delta_set/pmd:model_delta");
			modelDescXPath.setNamespaceURIs(namespaceMap);
			List results = modelDescXPath.selectNodes(deltaDoc);
			for (Iterator iter = results.iterator(); iter.hasNext(); ) {
				Element modelE = (Element) iter.next();
				String format = modelE.valueOf("@format");
				String targetName = modelE.valueOf("@target");
				System.out.println("Found model with target " + targetName + " and format " + format);
				if (format.equals("RDF/XML")) {
					Element modelRootElement = modelE.element("RDF");
					String modelXML = modelRootElement.asXML();
					System.out.println("Model text dump:\n===========================\n " + modelXML + "\n============================");
					StringReader mxsr = new StringReader(modelXML);
					String modelBaseURI = null;
					Model instructiveModel = ModelFactory.createDefaultModel();
					instructiveModel.read(mxsr, modelBaseURI, format);
					
					targetModel.add(instructiveModel);
					//mergeModelIntoDataSource(ds, inputName, instructiveModel);
				}
			}
			*/			
			/*
			String queryFileURL = "substrate/sparql/test/softy/fcq_01.sparql";
			String modelURL = "substrate/sparql/test/softy/facet_config_01.ttl";
			String modelBaseURI = "http://www.peruser.net/phonyBaseURI#";
			String modelFormat = "N3";
			
			String resultXML = SPARQL_Utils.executeQueryFromFiles (queryFileURL, modelURL, modelFormat, modelBaseURI);

			System.out.println(resultXML);
	*/


