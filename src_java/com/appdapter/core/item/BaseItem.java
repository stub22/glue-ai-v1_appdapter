/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.core.item;

import com.hp.hpl.jena.rdf.model.Literal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 */
public abstract class BaseItem implements Item {
	static Logger theLogger = LoggerFactory.getLogger(BaseItem.class);
	protected abstract Literal getLiteralVal(Ident fieldID, boolean throwOnFailure);

	protected abstract List<Item> getLinkedItems(Ident linkName);


	public int getLinkedItemCount(Ident linkName) {
		List<Item> linkedItems = getLinkedItems(linkName);
		return linkedItems.size();
	}

	public Set<Item> getLinkedItemSet(Ident linkName) {
		List<Item> linkedItems = getLinkedItems(linkName);
		Set s = new HashSet<Item>(linkedItems);
		return s;
	}

	public Item getSingleLinkedItem(Ident linkName) {
		List<Item> linkedItems = getLinkedItems(linkName);
		int size = linkedItems.size();
		if (size == 1) {
			return linkedItems.get(0);
		} else {
			throw new RuntimeException("Found " + size + " items instead of expected 1 at " + linkName);
		}
	}

	public List<Item> getSortedLinkedItemList(Ident linkName, List<SortKey> sortFieldNames) {
		theLogger.warn("These items are not really sorted for linkName: " + linkName);
		return getLinkedItems(linkName);
	}
	public Ident getValIdent(Ident fieldName) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getValDatatypeUri(Ident fieldID) {
		Literal lit = getLiteralVal(fieldID, true);
		return lit.getDatatypeURI();
	}
	@Override public Date getValDate(Ident fieldID, Date defaultVal) {
		Literal lit = getLiteralVal(fieldID, false);
		// return lit.get
		return defaultVal;
	}

	@Override public Double getValDouble(Ident fieldID, Double defaultVal) {
		Literal lit = getLiteralVal(fieldID, false);
		if (lit != null) {
			return lit.getDouble();
		}  else {
			return defaultVal;
		}
	}

	@Override public Long getValLong(Ident fieldID, Long defaultVal) {
		Literal lit = getLiteralVal(fieldID, false); 
		if (lit != null) {
			return lit.getLong();
		} else {
			return defaultVal;
		}
	}

	@Override public String getValString(Ident fieldID, String defaultVal) {
		Literal lit = getLiteralVal(fieldID, false);
		if (lit != null) {
			return lit.getString();
		} else {
			return defaultVal;
		}
	}

}
