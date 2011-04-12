package net.peruser.binding.jena;

import java.io.InputStream;
import java.io.PrintStream;

import java.net.URL;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.DataSource;

/** 
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class AssemblerUtils {
	private static Log 		theLog = LogFactory.getLog(AssemblerUtils.class );
	
	/**
	 *
	 */
	public static Model getAssembledModel (JenaKernel jk, String modelDescURI) throws Throwable {
		Resource desc = jk.findAssemblyResourceForFullURI(modelDescURI, false);
		// Mode.ANY allows the assember to reuse existing objects if possible, or create new ones where needed.
		Model result = (Model)Assembler.general.openModel(desc, Mode.ANY);
		return result;
	}

	public static Dataset getAssembledDataset (JenaKernel jk, String datasetDescURI) throws Throwable {
		
		/** 
	 	 * Note that schema for RDFDataset must be loaded to indicate that it is an assemblable "Object" 
		 * ja:RDFDataset  a rdfs:Class; rdfs:subClassOf ja:Object.
		 */
		Resource desc = jk.findAssemblyResourceForFullURI(datasetDescURI, false);
		// Mode.ANY allows the assember to reuse existing objects if possible, or create new ones where needed.
		DataSource ds = (DataSource) Assembler.general.open(desc); // , Mode.ANY);
		return ds;
	}	
	 
}

