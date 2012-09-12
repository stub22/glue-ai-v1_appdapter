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

import org.appdapter.core.name.Ident;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * An Item is something which has descriptive fields (attribs), and links to other items.
 * Fields must be functional (single-valued) properties.
 * Ordered collections are supported via getLinkedOrderedList and getSortedLinkedItemList methods.
 * If a set of item links has some natural order, this is simulated via a dummy sort field name (e.g. "rowNumber").
 */
public interface Item {
	public	Ident	getIdent();

	public	String getValString(Ident fieldIdent, String defaultVal);
	public	Long getValLong(Ident fieldIdent, Long defaultVal);
	public	Double getValDouble(Ident fieldIdent, Double defaultVal);
	public	Date getValDate(Ident fieldIdent, Date defaultVal);


	public Set<Item> getLinkedItemSet(Ident linkIdent);
	public int getLinkedItemCount(Ident linkIdent);
	public Item getSingleLinkedItem(Ident linkIdent);

	public static class SortKey {
		public	Ident		mySortFieldIdent;
		public enum Direction {
			ASCENDING, DESCENDING
		}
	}
	public List<Item> getLinkedItemsSorted(Ident linkIdent, List<SortKey> sortFieldNames);
	public List<Item> getLinkedOrderedList(Ident listLinkIdent);
}
