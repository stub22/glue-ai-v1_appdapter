/*
 *  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  See important notices at bottom of this file.
 */


package net.peruser.binding.jena;
import java.util.Iterator;
import java.util.List;

import java.io.FileInputStream;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
// import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.sparql.junit.*;

import com.hp.hpl.jena.query.DataSource;



import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetFactory;

import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;

/**
 * Certain static funcs, pulled directly from com/hp/hpl/jena/query/junit/QueryTest.java
 *
 * @author      Stu Baurmann of Scrutable, using code by Andy Seaborne of HP.
 * @version     @PERUSER_VERSION@
 * @copyright   derivative(HP, Scrutable)
 */
public class SPARQL_Utils {
    private static Log 		theLog = LogFactory.getLog(SPARQL_Utils.class );

	public static boolean isNonemptyList (List L) {
		return ((L != null) && (L.size() > 0));
	}

    public static boolean doesQueryHaveDataset(Query query) {
        return isNonemptyList(query.getGraphURIs()) || isNonemptyList(query.getNamedGraphURIs());
    }
    public static boolean doesTestItemHaveDataset(TestItem testItem)    {
        return isNonemptyList(testItem.getDefaultGraphURIs())  || isNonemptyList(testItem.getNamedGraphURIs());
    }	
    public static Dataset createDataset(List defaultGraphURIs, List namedGraphURIs)    {
        return DatasetUtils.createDataset(defaultGraphURIs, namedGraphURIs, null, null) ;
    }
    
    public static Model resultSetToModel(ResultSet rs) {
        Model m = GraphFactory.makeDefaultModel() ;
        ResultSetFormatter.asRDF(m, rs) ;
        if ( m.getNsPrefixURI("rs") == null ) {
            m.setNsPrefix("rs", com.hp.hpl.jena.vocabulary.ResultSet.getURI() ) ;
		}
        if ( m.getNsPrefixURI("rdf") == null ) {
            m.setNsPrefix("rdf", RDF.getURI() ) ;
		}
        if ( m.getNsPrefixURI("xsd") == null ) {
            m.setNsPrefix("xsd", XSDDatatype.XSD+"#") ;
		}
        return m;
    }
    
   static public boolean resultSetEquivalent(ResultSet rs1, ResultSet rs2) {
       Model model2 = resultSetToModel(rs2) ;
       return resultSetMatchesModel(rs1, model2) ;
   }

   static public boolean resultSetMatchesModel(ResultSet rs1, Model model2) {
       Model model1 = resultSetToModel(rs1) ;
       return model1.isIsomorphicWith(model2) ;
   }
	
	public static String execQueryToProduceXML(Model m, String qryString) {
		String resultXml;
		ResultSet rs = execQueryToProduceResultSet(m, qryString);
		ResultSetRewindable lastResultSet = ResultSetFactory.makeRewindable(rs);
		resultXml = ResultSetFormatter.asXMLString(lastResultSet);

		return resultXml;
	}

	public static ResultSet execQueryToProduceResultSet(Model m, String qryString) {
		Query qry = QueryFactory.create(qryString);
		QueryExecution qe = null;
		// 3rd arg is initial binding, currently unused.
		qe = QueryExecutionFactory.create(qry, m, null);
		ResultSet rs = qe.execSelect();
		
		return rs;
	}

	public static String executeQueryFromFiles (String queryFileURL, String modelURL, String modelFormat, String modelBaseURI) throws Throwable {
		FileInputStream	modelInputStream = new FileInputStream(modelURL);
		Model baseModel = ModelFactory.createDefaultModel();

		baseModel.read(modelInputStream, modelBaseURI, modelFormat);
		return executeQueryFromFile (queryFileURL, baseModel);
	}
	public static String executeQueryFromFile (String queryFileURL, Model inputModel) throws Throwable {
		
		Query parsedQuery = QueryFactory.read(queryFileURL, null, Syntax.syntaxSPARQL); // , queryBaseURI, queryFileSyntax);
		QueryExecution qe = QueryExecutionFactory.create(parsedQuery, inputModel, null);

		ResultSet rs = qe.execSelect();
		ResultSetRewindable lastResultSet = ResultSetFactory.makeRewindable(rs);
		String resultXML = ResultSetFormatter.asXMLString(lastResultSet);		
		return resultXML;
	}
	
	public static void dumpDatasetNames(Dataset dataset) throws Throwable {
		Iterator dsNameIterator = dataset.listNames();
		while (dsNameIterator.hasNext()) {
			String name = (String) dsNameIterator.next();
			theLog.debug("dataset contains model named: " + name);	
		}
	}
	public static String runQueryOverDataset (String queryFileURL, Dataset inputDataset) throws Throwable {
		
		Query parsedQuery = QueryFactory.read(queryFileURL, null, Syntax.syntaxSPARQL); // , queryBaseURI, queryFileSyntax);
		QueryExecution qe = QueryExecutionFactory.create(parsedQuery, inputDataset, null);

		ResultSet rs = qe.execSelect();
		ResultSetRewindable lastResultSet = ResultSetFactory.makeRewindable(rs);
		String resultXML = ResultSetFormatter.asXMLString(lastResultSet);		
		return resultXML;
	}

	/** 	
	  * The exact semantics here are still vague.   Roughly speaking, the new datasource
	  * is one that we can modify "without affecting" the original dataset, but that
	  * meaning is unclear when the original dataset contains database models.
	  * And even in the case of memory models, is Jena making a copy for us?
	  * Need to do some experimenting here.
	**/
	public static DataSource makeIndependentDataSourceFromDataset(Dataset dset) {
		Model newDefaultModel = null;
		Model oldDefaultModel = dset.getDefaultModel();
		if (oldDefaultModel != null) {
			newDefaultModel = ModelUtils.makeNaiveCopy(oldDefaultModel);
		}
		Dataset copy = DatasetFactory.make(dset, newDefaultModel);
		// We KNOW this is a DataSource from reading the ARQ 2.1 impl!!!  Ahem.
 		return (DataSource)  copy;
	}	
	
	/** 	
	**/
	public static void ensureDefaultModelNotNull(DataSource ds) {
		if (ds.getDefaultModel() == null) {
			Model emptyModel = ModelFactory.createDefaultModel();
			ds.setDefaultModel(emptyModel);
		}
	}
	
	/** 	If no model yet exists within dataset @ nameURI, then we create it.
			If nameURI is null, then we merge into (or create) the "default model"
	**/
	public static void mergeModelIntoDataSource(DataSource ds, String nameURI, Model m) {
		// Serializing model contents is expensive when the models aren't tiny.
		theLog.debug("SPARQL_Utils is merging in model with name " + nameURI); // + " and contents " + m);

		Model	previousModel = null;
		if (nameURI != null) {
			if (ds.containsNamedModel(nameURI)) {
				previousModel = ds.getNamedModel(nameURI);
			}
		} else {
			previousModel = ds.getDefaultModel();
		}
		Model augmentedModel = null;
		if (previousModel != null) {
			augmentedModel = previousModel;
			previousModel.add(m);
		} else {
			augmentedModel = m;
		}
		if (nameURI != null) {
			ds.replaceNamedModel(nameURI, augmentedModel);
		} else {
			ds.setDefaultModel(augmentedModel);
		}
	}
}


/**
 *  Note:  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  and is redistributed in MODIFIED form according to the terms of the following 
 *  license. 
 */
 
 /*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



