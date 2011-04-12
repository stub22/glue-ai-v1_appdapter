package net.peruser.binding.jena;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import net.peruser.module.projector.ProjectedNode;
import net.peruser.module.projector.Projector;
import net.peruser.module.projector.SimpleAxisQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class JenaPulljector implements Projector {
	
	private static Log 		theLog = LogFactory.getLog(JenaPulljector.class);	
	
	private	Model		myModel;
	public JenaPulljector (Model m) {
		myModel = m;
	}
	public ProjectedNode projectNode(String uriString, Set axisQueries) throws Throwable {
		// For now, we assume that all axisQueries are simple, but we might grow here later.
		
		// Our task is to translate the abstract query we've been given into something that
		// can be executed against our Jena model.  
		Resource r = myModel.getResource(uriString);
		ArrayList jenaAxisQueries = new ArrayList();
		Iterator aqi = axisQueries.iterator();
		while (aqi.hasNext()) {
			SimpleAxisQuery saq = (SimpleAxisQuery) aqi.next();
			JenaSimpleAxisQuery jsaq = new JenaSimpleAxisQuery();
			
			String propertyURI = saq.getPropertyURI();
			jsaq.property = resolveProperty(propertyURI);
			jsaq.direction = saq.getDirection();
			
			jenaAxisQueries.add(jsaq);
		}
		ProjectedNode jpn = new JenaPullNode(this, r, jenaAxisQueries, null);
		return jpn;
	}
	Model getModel() {
		return myModel; 
	}
	Property resolveProperty(String uri) throws Throwable {
		Resource propResource = myModel.getResource(uri);
		Property prop = (Property) propResource.as(Property.class);
		return prop;
	}

	public static JenaPulljector makePulljectorFromBaseModelAndOntSpec (Model baseModel,
			 OntModelSpec ontModelSpec) throws Throwable {
	
		Model pulljectorModel = baseModel;
		if (ontModelSpec != null) {
			OntModel ontModel = ModelFactory.createOntologyModel(ontModelSpec, baseModel);
			pulljectorModel = ontModel;
		}
		JenaPulljector	jp = new JenaPulljector(pulljectorModel);
		return jp;
	}

	public  void dumpPrefixes(PrintStream out) throws Throwable {
		Map pmap = myModel.getNsPrefixMap();
		Iterator pki = pmap.keySet().iterator();
		while (pki.hasNext()) {
			String key = (String) pki.next();
			String val = (String) pmap.get(key);
			theLog.debug("Prefix " + key + " is mapped to URI " + val);
		}
	}

	
	public static class JenaPullNode implements ProjectedNode {
		private		JenaPulljector	myPulljector;
		private		Resource		myResource;
		private		List			myJenaAxisQueries;
		private		Property		myStemProperty;
		
		public JenaPullNode (JenaPulljector jp, Resource r, List jenaAxisQueries, Property stemProperty) {
			myPulljector = jp;
			myResource = r;
			myJenaAxisQueries = jenaAxisQueries;
			myStemProperty = stemProperty;
		}
		public Projector	getProjector() throws Throwable {
			return myPulljector;
		}
		public String		getUriString() throws Throwable {
			return myResource.getURI();
		}
		public Iterator		getTypeUriStringIterator() throws Throwable {
			return null;
		}
		public String getStemPropertyURI()  throws Throwable {
			String result = "NO_STEM";
			if (myStemProperty != null) {
				result = myStemProperty.getURI();
			}
			return result;
		}
	
		public Iterator		getFieldValueStringIterator(String propertyURI) throws Throwable {			
			Property	prop = myPulljector.resolveProperty(propertyURI);
			ArrayList 	values = new ArrayList();
			StmtIterator sit = myResource.listProperties(prop); 
			while (sit.hasNext()) {
				String valueString = null;
				Statement s = sit.nextStatement();
				RDFNode	so = s.getObject();
				if (so.canAs(Literal.class)) {
					Literal lito = (Literal) so.as(Literal.class);
					valueString = lito.getLexicalForm();
				} else {
					Resource r = (Resource) so.as(Resource.class);
					valueString = r.getURI();
				}
				values.add(valueString);
			}
			return values.iterator();
		}
		public Iterator		getChildNodeIterator() throws Throwable {
			// Big question is what we want to do with duplicates?
			// Remember, we're really traversing a graph!
			
			// We currently will silently accept duplicates into the result list, which is not correct.
			
			HashSet		children = new HashSet();
			Iterator jaqi = myJenaAxisQueries.iterator(); 
			while (jaqi.hasNext()) {
				JenaSimpleAxisQuery jsaq = (JenaSimpleAxisQuery) jaqi.next();
				Property qp = jsaq.property;				   
				Resource 	qs = null;
				Resource 	qo = null;
				Model		qm = myResource.getModel();
				if (jsaq.direction == SimpleAxisQuery.PARENT_POINTS_TO_CHILD) {
					qs = myResource;
				} else if (jsaq.direction == SimpleAxisQuery.CHILD_POINTS_TO_PARENT) {
					qo = myResource;
				} else {
					throw new Exception("[JenaPullNode.getChildNodeIterator()] Found illegal direction " + jsaq.direction);
				}
				StmtIterator sit = qm.listStatements(qs, qp, qo);
				while (sit.hasNext()) {
					Statement resultStatement = sit.nextStatement();
					Resource childResource = null;
					if (jsaq.direction == SimpleAxisQuery.PARENT_POINTS_TO_CHILD) {
						// This gets the object of the result statement as a resource, or throws on a literal
						childResource = resultStatement.getResource();
					} else if (jsaq.direction == SimpleAxisQuery.CHILD_POINTS_TO_PARENT) {
						childResource = resultStatement.getSubject();
					}
					// System.out.println("found childResource: " + childResource.getURI());
					// Ignore reflexive hits, e.g. "A subClassOf A"
					if (!childResource.equals(myResource)) {
						JenaPullNode childNode = new JenaPullNode (myPulljector, childResource, myJenaAxisQueries, qp);
						children.add(childNode);
					}

				}
				sit.close();
			}
			// System.out.println("getChildNodeIterator() found " + children.size() + " kids");
			return children.iterator();
		}
	}
	public static class JenaSimpleAxisQuery {
		public Property property;
		public int direction;
	}	
}
