package net.peruser.binding.jena;

import java.io.InputStream;
import java.io.PrintStream;

import java.net.URL;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

// See note below under loadJenaModelUsingJenaFileManager()
// import com.hp.hpl.jena.sparql.util.RelURI;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.peruser.core.environment.Environment;

// import static net.peruser.test.data.TestDataConstants.ModelUtilsUnitTestConstants.*;

/* Got these refs from Manifest.java */

/** 
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ModelUtils
{
	private static Log 		theLog = LogFactory.getLog(ModelUtils.class);
		
	public static String getStringPropertyValue(Individual i, DatatypeProperty dp) throws Throwable {
		String result = null;
		RDFNode rdfnode = i.getPropertyValue(dp);
		if (rdfnode != null) {
			Literal lit = (Literal) rdfnode.as(Literal.class);
			result = lit.getString();
		}
		return result;
	}
	public static Individual getChildIndividual (Individual p, ObjectProperty op) throws Throwable {
		Individual result = null;
		RDFNode childRDFNode = p.getPropertyValue(op);
		if (childRDFNode != null) {
			result = (Individual) childRDFNode.as(Individual.class);
		}		
		return result;
	}
	
	public static void printIndividualDebug(Individual i) {
		theLog.debug("individual.toString(): " + i.toString());
		theLog.debug("individual.localName(): " + i.getLocalName());
		ExtendedIterator it = i.listRDFTypes(false);
		while (it.hasNext()) {
			Object o = it.next();
			logDebug ("PSOP-INFO: Found RDF:type with java-class, toString(): " 
				+ o.getClass().getName() + ", "  + o);
		}
		StmtIterator pi = i.listProperties();
		while (pi.hasNext()) {
			Statement s = pi.nextStatement();
			logDebug("PSOP-INFO: Found op property statement: " + PrintUtil.print (s));
		}
	}

	public static void printOntClassStats (OntModel om, String classURI) {
		theLog.debug("classURI=" + classURI);
		OntClass ontClass = om.getOntClass(classURI);
		if (ontClass != null) {
			printIterator(ontClass.listSuperClasses(), "All super classes of " + ontClass.getLocalName());
			printIterator(ontClass.listSuperClasses(true), "Direct superclasses of " + ontClass.getLocalName());
		} else {
			logDebug("printOntClassStats-ERROR, can't find ontclass at " + classURI);
		}
	}
    
    public static void printIterator(Iterator i, String header) {
		StringBuffer buf = new StringBuffer(header);
    
        for(int c = 0; c < header.length(); c++) {
            buf.append("=");
		}
        logDebug(buf.toString());
		
        if(i.hasNext()) {
	        while (i.hasNext())  {
	            logDebug( i.next().toString() );
			}
        }       
        else {
            logDebug("<EMPTY>");
        }
		logDebug("");
    }
	public static String getServletContextPath (HttpServlet servlet, String relativePath) {
		return null;
	}
    public static URL getResourceURL (String resourcePath, Class resourceOwner) throws Throwable {
		ClassLoader loader = resourceOwner.getClassLoader();
        logDebug("[SubstrateOperationUtils.getResourceURL] seeking " + resourcePath);
        URL resourceURL = loader.getResource (resourcePath);
		if( resourceURL == null ) {
            logError("[SubstrateOperationUtils.getResourceURL] FAILED TO FIND : " + resourcePath);
        }
        else {
            logDebug("[SubstrateOperationUtils.getResourceURL] got resource url: " + resourceURL);
        }
		return resourceURL;
	}
	public static void logDebug(String s) {
		theLog.debug(s);
	}
	public static void logError(String s) {
		theLog.error(s);
	}
	
	static public  void dumpPrefixes(Model m) throws Throwable {
		Map pmap = m.getNsPrefixMap();
		Iterator pki = pmap.keySet().iterator();
		while (pki.hasNext()) {
			String key = (String) pki.next();
			String val = (String) pmap.get(key);
			logDebug ("Prefix " + key + " is mapped to URI " + val);
		}
	}
	static public Model loadJenaModelUsingJenaFileManager (Environment env, String relativePath) throws Throwable {
		theLog.debug("******************   ModelUtils.loadJenaModelUsingJenaFileManager() - relativePath:  " + relativePath);
		/* This is similar to the code in jena's Manifest.java */
	    Model loadedModel = null;
		JenaKernel	jk = JenaKernel.getDefaultKernel (env);
		loadedModel = jk.getBestModelForLocation(relativePath);
		//  This (normally commented out) line will serialize the model, which is expensive if the model is not tiny.
		//  theLog.debug("******************   ModelUtils.loadJenaModelUsingJenaFileManager() - loadedModel:  " + loadedModel);		
		return loadedModel;
		/*
		RelURI disappeared between Jena 2.5.2 and 2.5.4.  Appears that FileManager.mapURI may be the closest replacement.
        String fullPath = RelURI.resolve(relativePath) ;
        logDebug("JFM resolved " + relativePath + " to " + fullPath) ;
		FileManager globalFileManager = FileManager.get();
        loadedModel = globalFileManager.loadModel(fullPath) ;
		return loadedModel;
		*/
	}
	/**
	 * From jena docs for model.read()
	 *  base - the base to use when converting relative to absolute uri's. The base URI may be null if there are 
	 * no relative URIs to convert. A base URI of "" may permit relative URIs to be used in the model unconverted.
	 */
	static public Model loadJenaModelFromXmlSerialStream (InputStream xmlInputStream, 
				String modelBaseURI) throws Throwable {
		Model model = ModelFactory.createDefaultModel();
		// This form of Model.read() assumes that the model is encoded as RDF/XML.
		model.read(xmlInputStream, modelBaseURI);
		return model;
	}
	static public OntModel loadRDFS_ModelFromStream (InputStream modelInputStream, String modelBaseURI) throws Throwable {
		Model baseModel = ModelFactory.createDefaultModel();
		baseModel.read(modelInputStream, modelBaseURI);
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, baseModel);
		return ontModel;
	}
	
	
	/*
	        StmtIterator sIter = model.listStatements(null, RDF.type, type) ;
        if ( ! sIter.hasNext() )
            return null ;
        Resource r = sIter.nextStatement().getSubject() ;
	
	
	*/

	  /*
	public static OntDocumentManager TEST_getOntDocumentManager () {
		OntDocumentManager odm = OntDocumentManager.getInstance();

		odm.addAltEntry(dhuOntURI, dhuOntURL);
		odm.addAltEntry(dhdOntURI, dhdOntURL);
		odm.addAltEntry(subOntURI, subOntURL);
		
		return odm;
	}
	*/
	
/**
	Ignores a lot of important issues, such as what type the input model is, whether it contains reified statements, etc.
 */
 	public static Model makeNaiveCopy(Model in) {
		Model 	out = ModelFactory.createDefaultModel();
		out.add(in);
		return out;
 	}
	
	public static  Resource makeUnattachedResource (String uri) {
		return ResourceFactory.createResource(uri);
	}

}

