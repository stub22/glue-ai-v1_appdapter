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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.appdapter.core.name.Ident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Literal;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * TODO:  BaseItem can extend BaseIdent to ensure we can have same hashCode() + equals() for all idents + items.
 */
public abstract class BaseItem implements Item {
	static Logger theLogger = LoggerFactory.getLogger(BaseItem.class);
	protected abstract Literal getLiteralVal(Ident fieldID, boolean throwOnFailure);

	protected abstract List<Item> getLinkedItems(Ident linkName);


	public int getLinkedItemCount(Ident linkName) {
		Collection<Item> linkedItems = getLinkedItems(linkName);
		return linkedItems.size();
	}

	public Set<Item> getLinkedItemSet(Ident linkName) {
		Collection<Item> linkedItems = getLinkedItems(linkName);
		Set s = new HashSet<Item>(linkedItems);
		return s;
	}

	public Item getSingleLinkedItem(Ident linkName) {
		Collection<Item> linkedItems = getLinkedItems(linkName);
		int size = linkedItems.size();
		if (size == 1) {
			Item items[] = new Item[1];
			linkedItems.toArray(items);
			return items[0];
		} else {
			throw new RuntimeException("Found " + size + " items instead of expected 1 at " + linkName);
		}
	}
	/**
	 * This implementation does not yet actually do the requested sorting.
	 * To be fixed!
	 * @param linkName
	 * @param sortFieldNames - presently ignored.
	 * @return 
	 */
	@Override public List<Item> getLinkedItemsSorted(Ident linkName, List<SortKey> sortFieldNames) {
		// theLogger.warn("These items are not yet really sorted by linkName: " + linkName);
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
		throw new UnsupportedOperationException("Date literals not supported yet.");
		//Literal lit = getLiteralVal(fieldID, false);
		// return lit.get
		//return defaultVal;
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
