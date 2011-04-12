package net.peruser.module.absorber;

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
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;

import com.hp.hpl.jena.sparql.util.TypeNotUniqueException;
import com.hp.hpl.jena.util.FileManager;



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

import net.peruser.core.command.AbstractCommand;

import net.peruser.core.config.Config;

import net.peruser.core.environment.Environment;

import net.peruser.core.name.Address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import org.dom4j.io.SAXReader;

/**  AbsorberTest is our unit test for the peruser "Absorber" model updater.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class AbsorberTest_scrutable {
	public static void main(String[] args) {
		System.out.println("AbsorberTest - inflating!");		
			// String 	inputDataFile = args[0];
		String	assemblerFile = "app/picky/rdf/picky_sql_model_assembly.ttl";
		String  assemblerResURI = "http://www.peruser.net/picky#camera_features_001_db_model";
		String  deltaDocPath = "file:app/picky/test_xml/amazonCameraFeatureModel_bigger_sample.xml";
			
		Model targetModel = null;
		
        try {
            Model assemblerSpecModel = null ;
      
			assemblerSpecModel = FileManager.get().loadModel(assemblerFile) ;
  
			Resource dbModelAssemblerDesc = assemblerSpecModel.createResource(assemblerResURI);
			// Mode.ANY allows the assember to reuse existing objects if possible, or create new ones where needed.
			targetModel = (Model)Assembler.general.openModel(dbModelAssemblerDesc, Mode.ANY);			

			SAXReader reader = new SAXReader();
			Document deltaDoc = reader.read(deltaDocPath);

			// This query does not get the benefit of inference...
			// dbModelAssemblerDesc = GraphUtils.getResourceByType(spec, JA.RDBModel) ;
			
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
			/*
			String queryFileURL = "substrate/sparql/test/softy/fcq_01.sparql";
			String modelURL = "substrate/sparql/test/softy/facet_config_01.ttl";
			String modelBaseURI = "http://www.peruser.net/phonyBaseURI#";
			String modelFormat = "N3";
			
			String resultXML = SPARQL_Utils.executeQueryFromFiles (queryFileURL, modelURL, modelFormat, modelBaseURI);

			System.out.println(resultXML);
	*/
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("AbsorberTest - deflating!");
	}

}
