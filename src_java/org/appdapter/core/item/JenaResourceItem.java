/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appdapter.core.name.Ident;
import org.appdapter.core.name.ModelIdent;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We rely on the ability of myResource to perform getModel().
 * 
 * Resources created by ResourceFactory will not refer to any model, 
 * and will not permit operations which require 
 * a model. Such resources are useful as general constants.
 */
public class JenaResourceItem extends BaseItem implements ModelIdent {
	private static Logger theLogger = LoggerFactory.getLogger(JenaResourceItem.class);
	private transient Resource myResource;

	protected JenaResourceItem() {
		
	}
	public JenaResourceItem(Resource r) {
		if (r == null) {
			throw new RuntimeException("Cannot create JenaResourceItem with null resource.");
		}
		myResource = r;
	}
/**
 * We attempt to compare based on the absUriString.
 * @param o
 * @return 
 */
	@Override public boolean equals(Object o) {
		if ((o != null) && (o instanceof Ident)) {
			String absUri = getAbsUriString();
			if (absUri != null) {
				String otherAbsUri = ((Ident) o).getAbsUriString();
				return getAbsUriString().equals(otherAbsUri);
			} else {
				if (o instanceof JenaResourceItem) {
					myResource.equals(((JenaResourceItem) o).myResource);
				}
			}
		}
		return false;
	}


	@Override public int hashCode() {
		// Blank-nodes do not have an absUriString
		String uriString = getAbsUriString();
		if (uriString != null) {
			return getAbsUriString().hashCode();
		} else {
			return myResource.hashCode();
		}
	}

	/**
	 * 
	 * @return  this same object (This item *is* an ident!)
	 */
	@Override public Ident getIdent() {
		return this;
	}

	final @Override public String toString() {
		return "JenaResourceItem[res=" + myResource.toString() + "]";
	}

	/**
	 * If there *is* no model, then we return another resource that has no model.
	 * @param absURI
	 * @return 
	 */
	@Override public Ident getIdentInSameModel(String absURI) {
		Model m = getModel();
		Resource createdRes = null;
		if (m != null) {
			createdRes = m.createResource(absURI);
		} else {
			createdRes = ResourceFactory.createResource(absURI);
		}
		JenaResourceItem itemInSameJClazz = makeItemOfMyJClazz(createdRes);
		return itemInSameJClazz;
	}

	public String getAbsUriString() {
		Resource mr = getJenaResource();
		return mr.getURI();
	}

	public String getLocalName() {
		Resource mr = getJenaResource();
		return mr.getLocalName();
	}

	public Map<Property, List<RDFNode>> getPropertyMap() {
		Model model = getModel();
		if (model == null) {
			throw new RuntimeException("This item is not connected to a model: " + this.toString());
		}
		Map<Property, List<RDFNode>> properties = new HashMap<Property, List<RDFNode>>();
		StmtIterator iter = model.listStatements();
		Resource meRes = getJenaResource();
		// TODO this is slow
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			if (subject.equals(meRes)) {
				Property predicate = stmt.getPredicate(); // get the predicate
				List<RDFNode> results = properties.get(predicate);
				if (results == null) {
					results = new ArrayList<RDFNode>();
					properties.put(predicate, results);
				}
				RDFNode object = stmt.getObject(); // get the object
				results.add(object);
			}
		}
		return properties;
	}

	protected List<RDFNode> getPropertyValues(Ident fieldID, LinkDirection linkDir) {
		List<RDFNode> results = new ArrayList<RDFNode>();
		Model model = getModel();
		if (model == null) {
			throw new RuntimeException("This item is not connected to a model: " + this.toString());
		}		
		Resource fieldPropertyRes = null;
		Resource meRes = getJenaResource();
		if (fieldID instanceof JenaResourceItem) {
			fieldPropertyRes = ((JenaResourceItem) fieldID).getJenaResource();
		} else {
			String fieldPropURI = fieldID.getAbsUriString();
			if (fieldPropURI != null) {
				fieldPropertyRes = model.createResource(fieldPropURI);
			}
		}
		if (fieldPropertyRes != null) {
			Property fieldProperty = fieldPropertyRes.as(Property.class);
		
			if (linkDir == LinkDirection.FORWARD) {
				NodeIterator nodeIt = model.listObjectsOfProperty(meRes, fieldProperty);
				while (nodeIt.hasNext()) {
					results.add(nodeIt.nextNode());
				}
			} else if (linkDir == LinkDirection.REVERSE) {
				ResIterator resIt = model.listResourcesWithProperty(fieldProperty, meRes);
				while (resIt.hasNext()) {
					results.add(resIt.nextResource());
				}
			} 
		}
		return results;
	}

	public Model getModel() {
		return getJenaResource().getModel();
	}

	protected RDFNode getSinglePropertyVal(Ident fieldID, boolean throwOnFailure) {
		List<RDFNode> nodes = getPropertyValues(fieldID, Item.LinkDirection.FORWARD);
		int nodeListSize = nodes.size();
		if (nodeListSize == 1) {
			return nodes.get(0);
		} else if (throwOnFailure) {
			throw new RuntimeException("Got " + nodeListSize + " nodes instead of expected 1 for " + fieldID);
		} else {
			return null;
		}
	}

	@Override protected Literal getLiteralVal(Ident fieldID, boolean throwOnFailure) {
		Literal resultLit = null;
		RDFNode resultNode = getSinglePropertyVal(fieldID, throwOnFailure);
		if (resultNode != null) {
			resultLit = resultNode.asLiteral();
		}
		return resultLit;
	}

	@Override protected List<Item> getLinkedItems(Ident linkName, LinkDirection linkDir) {
		List<RDFNode> linkedNodes;
		linkedNodes = getPropertyValues(linkName, linkDir);
		List<Item> results = new ArrayList<Item>();
		for (RDFNode rn : linkedNodes) {
			Resource res = rn.asResource();
			JenaResourceItem jri =  makeItemOfMyJClazz(res); //  new JenaResourceItem(res);
			results.add(jri);
		}
		return results;
	}

	@Override public List<Item> getLinkedOrderedList(Ident linkName) {
		List<Item> results = new ArrayList<Item>();
		RDFNode resultNode = getSinglePropertyVal(linkName, false);
		if (resultNode != null) {
			RDFList rdfList = resultNode.as(RDFList.class);
			//System.out.println("Found rdfList[" + linkName + "] = " + rdfList);
			if (rdfList != null) {
				List<RDFNode> javaNodeList = rdfList.asJavaList();
				// System.out.println("JavaNodeList = " + javaNodeList);
				for (RDFNode elementNode : javaNodeList) {
					Resource res = elementNode.asResource();
					JenaResourceItem jri = makeItemOfMyJClazz(res); // new JenaResourceItem(res);
					results.add(jri);
				}
			}
		}
		return results;

	}
	public Resource getJenaResource() {
		return myResource;
	}
	protected void setJenaResource(Resource r) {
		myResource = r;
	}
	protected JenaResourceItem makeItemOfMyJClazz(Resource res) {
		return new JenaResourceItem(res);
	}
	
}
