/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.core.item;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author winston
 */
public class JenaResourceItem extends BaseItem implements ModelIdent {
	// We rely on the ability of the resource to perform getModel().
	Resource	myResource;
	public JenaResourceItem(Resource r) {
		if (r == null) {
			throw new RuntimeException("Cannot create JenaResourceItem with null resource.");
		}
		myResource = r;
	}
	@Override public boolean equals(Object o) {
		if (o instanceof JenaResourceItem) {
			return myResource.equals(((JenaResourceItem) o).myResource);
		} else {
			return false;
		}
	}
	@Override public int hashCode() {
		return myResource.hashCode();
	}
	@Override public Ident getIdent() {
		return this;
	}
	@Override public String toString() {
		return "JenaResourceItem[res=" + myResource.toString() + "]";
	}
	@Override public Ident getIdentInSameModel(String absURI) {
		Resource res = myResource.getModel().createResource(absURI);
		return new JenaResourceItem(res);
	}
	public String getAbsUriString() {
		return myResource.getURI();
	}

	public String getLocalName() {
		return myResource.getLocalName();
	}
	protected List<RDFNode> getPropertyValues(Ident fieldID) {
		List<RDFNode> results = new ArrayList<RDFNode>();
		Model	model = myResource.getModel();
		Resource	fieldPropertyRes = null;
		if (fieldID instanceof JenaResourceItem) {
			fieldPropertyRes = ((JenaResourceItem) fieldID).myResource;
		} else {
			String fieldPropURI = fieldID.getAbsUriString();
			if (fieldPropURI != null) {
				fieldPropertyRes = model.createResource(fieldPropURI);
			}
		}
		if (fieldPropertyRes != null) {
			Property fieldProperty = fieldPropertyRes.as(Property.class);
			NodeIterator nodeIt = model.listObjectsOfProperty(myResource, fieldProperty);
			while(nodeIt.hasNext()) {
				results.add(nodeIt.nextNode());
			}
		}
		return results;
	}
	@Override protected Literal getLiteralVal(Ident fieldID, boolean throwOnFailure) {
		List<RDFNode> nodes = getPropertyValues(fieldID);
		int nodeListSize = nodes.size();
		if (nodeListSize == 1) {
			RDFNode firstNode = nodes.get(0);
			return firstNode.asLiteral();
		} else if (throwOnFailure) {
			throw new RuntimeException("Got " + nodeListSize + " nodes instead of expected 1 for " + fieldID);
		} else {
			return null;
		}
	}
	@Override protected List<Item> getLinkedItems(Ident linkName) {
		List<RDFNode> nodes = getPropertyValues(linkName);
		List<Item> results = new ArrayList<Item>();
		for (RDFNode rn : nodes) {
			Resource res = rn.asResource();
			JenaResourceItem jri = new JenaResourceItem(res);
			results.add(jri);
		}
		return results;
	}

	public Resource getJenaResource() {
		return myResource;
	}

}
