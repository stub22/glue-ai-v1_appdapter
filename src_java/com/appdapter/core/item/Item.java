/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.core.item;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author winston
 * An Item is something which has descriptive fields (attribs), and links to other items.
 * Fields must be functional (single-valued) properties.
 * Ordered collections are supported only via the getSortedLinkedItemList method.
 * If a set of item links has some natural order, this is simulated via a dummy sort field name (e.g. "rowNumber").
 */
public interface Item {
	public	Ident	getIdent();

	public	String getValString(Ident fieldName, String defaultVal);
	public	Long getValLong(Ident fieldName, Long defaultVal);
	public	Double getValDouble(Ident fieldName, Double defaultVal);
	public	Date getValDate(Ident fieldName, Date defaultVal);


	public Set<Item> getLinkedItemSet(Ident linkName);
	public int getLinkedItemCount(Ident linkName);
	public Item getSingleLinkedItem(Ident linkName);

	public static class SortKey {
		public	Ident		mySortFieldName;
		public enum Direction {
			ASCENDING, DESCENDING
		}
	}
	public List<Item> getSortedLinkedItemList(Ident linkName, List<SortKey> sortFieldNames);
}
