/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.rdf.jena.model;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.core.convert.Converter.ConverterMethod;
import org.appdapter.core.convert.OptionalArg;
import org.appdapter.core.debug.UIAnnotations.UISalient;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * @author Stu B. <www.texpedient.com>
 */
@UISalient
public class JenaModelUtils implements AnyOper.UtilClass {
	private static Logger theLogger = LoggerFactory.getLogger(JenaModelUtils.class);

	public static void logDebug(String s) {
		theLogger.debug(s);
	}

	public static void logError(String s) {
		theLogger.error(s);
	}

	public static String getStringPropertyValue(Individual i, DatatypeProperty dp) throws Throwable {
		String result = null;
		RDFNode rdfnode = i.getPropertyValue(dp);
		if (rdfnode != null) {
			Literal lit = (Literal) rdfnode.as(Literal.class);
			result = lit.getString();
		}
		return result;
	}

	public static Individual getChildIndividual(Individual p, ObjectProperty op) throws Throwable {
		Individual result = null;
		RDFNode childRDFNode = p.getPropertyValue(op);
		if (childRDFNode != null) {
			result = (Individual) childRDFNode.as(Individual.class);
		}
		return result;
	}

	static public void dumpPrefixes(Model m) throws Throwable {
		Map pmap = m.getNsPrefixMap();
		Iterator pki = pmap.keySet().iterator();
		while (pki.hasNext()) {
			String key = (String) pki.next();
			String val = (String) pmap.get(key);
			logDebug("Prefix " + key + " is mapped to URI " + val);
		}
	}

	public static void printIndividualDebug(Individual i) {
		logDebug("individual.toString(): " + i.toString());
		logDebug("individual.localName(): " + i.getLocalName());
		ExtendedIterator it = i.listRDFTypes(false);
		while (it.hasNext()) {
			Object o = it.next();
			logDebug("PSOP-INFO: Found RDF:type with java-class, toString(): " + o.getClass().getName() + ", " + o);
		}
		StmtIterator pi = i.listProperties();
		while (pi.hasNext()) {
			Statement s = pi.nextStatement();
			logDebug("PSOP-INFO: Found op property statement: " + PrintUtil.print(s));
		}
	}

	public static void printOntClassStats(OntModel om, String classURI) {
		logDebug("classURI=" + classURI);
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

		for (int c = 0; c < header.length(); c++) {
			buf.append("=");
		}
		logDebug(buf.toString());

		if (i.hasNext()) {
			while (i.hasNext()) {
				logDebug(i.next().toString());
			}
		} else {
			logDebug("<EMPTY>");
		}
		logDebug("");
	}

	public static URL getResourceURL(String resourcePath, Class resourceOwner) throws Throwable {
		ClassLoader loader = resourceOwner.getClassLoader();
		logDebug("[JenaModelUtils.getResourceURL] seeking " + resourcePath);
		URL resourceURL = loader.getResource(resourcePath);
		if (resourceURL == null) {
			logError("[JenaModelUtils.getResourceURL] FAILED TO FIND : " + resourcePath);
		} else {
			logDebug("[JenaModelUtils.getResourceURL] got resource url: " + resourceURL);
		}
		return resourceURL;
	}

	/**
	 * From jena docs for model.read()
	 *  base - the base to use when converting relative to absolute uri's. The base URI may be null if there are
	 * no relative URIs to convert. A base URI of "" may permit relative URIs to be used in the model unconverted.
	 */
	static public Model loadJenaModelFromXmlSerialStream(InputStream xmlInputStream, String modelBaseURI) throws Throwable {
		Model model = RepoDatasetFactory.createDefaultModel();
		// This form of Model.read() assumes that the model is encoded as RDF/XML.
		model.read(xmlInputStream, modelBaseURI);
		return model;
	}

	static public OntModel loadRDFS_ModelFromStream(InputStream modelInputStream, String modelBaseURI) throws Throwable {
		Model baseModel = RepoDatasetFactory.createDefaultModel();
		baseModel.read(modelInputStream, modelBaseURI);
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, baseModel);
		return ontModel;
	}

	public static Model makeNaiveCopy(Model in) {
		Model out = RepoDatasetFactory.createDefaultModel();
		out.add(in);
		return out;
	}

	@UISalient(UseOptionalArgs = OptionalArg.OPTIONAL_FROM_CLIPBOARD)//
	public static List<Statement> findExentOfNodeInModels(Resource r, Collection<Model> searchThese) {
		ArrayList<Statement> found = new ArrayList<Statement>();
		Collection<Model> foundIn = findModelsWithNode(r, searchThese);
		final Node n0 = r.asNode();
		for (Model m : foundIn) {
			for (Statement s : m.listStatements().filterKeep(new Filter<Statement>() {

				@Override public boolean accept(Statement arg0) {
					if (n0.equals(arg0.getSubject().asNode()))
						return true;
					if (n0.equals(arg0.getPredicate().asNode()))
						return true;
					if (n0.equals(arg0.getObject().asNode()))
						return true;
					return false;
				}
			}).toList()) {
				found.add(s);
			}
		}
		return found;

	}

	@UISalient() public static List<Model> findModelsWithNode(Resource r, Collection<Model> searchThese) {
		ArrayList<Model> foundIn = new ArrayList<Model>();
		for (Model m : searchThese) {
			if (m.containsResource(r)) {
				foundIn.add(m);
			}
		}
		return foundIn;
	}

	@ConverterMethod public static Resource makeUnattachedResource(String uri) {
		return ResourceFactory.createResource(uri);
	}

	@ConverterMethod public static Model toModel(JenaResourceItem item) {
		return item.getModel();
	}

	@ConverterMethod public static Resource toResource(JenaResourceItem item) {
		return item.getJenaResource();
	}

	@ConverterMethod public static Model toModel(Resource item) {
		return item.getModel();
	}

	@ConverterMethod public static Graph toGraph(Model item) {
		return item.getGraph();
	}

	@ConverterMethod public static Collection<Statement> toStatements(Model item) {
		return item.listStatements().toList();
	}

	@ConverterMethod public static Ident toIdent(Resource item) {
		return new JenaResourceItem(item);
	}
}
