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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * An Item is something which has descriptive fields (attribs), and links to other items.
 * Fields must be functional (single-valued) properties.
 * Ordered collections are supported via getLinkedOrderedList and getSortedLinkedItemList methods.
 * If a set of item links has some natural order, this is simulated via a dummy sort field name (e.g. "rowNumber").
 * 
 * When an Item is taken from an RDF graph (e.g. a JenaResourceItem), then it is a view of that graph's 
 * statements as fields related to our *ident*.    So we have a subtle relationship between the container
 * (the Item, which is not explicitly manifest in the RDF graph) and the name of the container (which appears
 * multiple times in the RDF graph - in every statement describing the resource).  
 */
public interface Item {
	/**
	 * In some cases (e.g. JenaResourceItem), this Ident may turn out to be the same java object as the Item itself.
	 * 
	 * @return 
	 */
	public	Ident	getIdent();

	public	String getValString(Ident fieldIdent, String defaultVal);
	public	Long getValLong(Ident fieldIdent, Long defaultVal);
	public	Integer getValInteger(Ident fieldIdent, Integer defaultVal);
	public	Double getValDouble(Ident fieldIdent, Double defaultVal);
	public	Date getValDate(Ident fieldIdent, Date defaultVal);
	public	Boolean getValBoolean(Ident fieldIdent, Boolean defaultVal);

	public enum LinkDirection {
		FORWARD,
		REVERSE
	}
	// Currently we assume forward links
	// TODO:  Add ability to find items linked to this one ("reverse" linked), which should 
	// allow us to find 1) children using a parent-link, and 2) instances of a type (reverse linked to rdf:type).  
	public Set<Item> getLinkedItemSet(Ident linkIdent, LinkDirection linkDir);
	public int getLinkedItemCount(Ident linkIdent, LinkDirection linkDir);
	public Item getSingleLinkedItem(Ident linkIdent, LinkDirection linkDir);
	public Item getOptionalSingleLinkedItem(Ident linkIdent, LinkDirection linkDir);

	public static class SortKey {
		public	Ident		mySortFieldIdent;
		public enum Direction {
			ASCENDING, DESCENDING
		}
	}
	public List<Item> getLinkedItemsSorted(Ident linkIdent, LinkDirection linkDir, List<SortKey> sortFieldNames);
	
	// Under Jena, expects to map to RDF:Collection property
	public List<Item> getLinkedOrderedList(Ident listLinkIdent);
}
