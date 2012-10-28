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
package org.appdapter.core.item;

import org.appdapter.core.name.Ident;
import org.appdapter.core.name.ModelIdent;
import java.util.Set;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ItemFuncs {
	public static Ident getNeighborIdent(Ident neighborIdent, String fieldName) {
		Ident result = null;
		if ((neighborIdent != null) &&  (neighborIdent instanceof ModelIdent)) {
			ModelIdent neighborModelIdent = (ModelIdent) neighborIdent;
			result = neighborModelIdent.getIdentInSameModel(fieldName);
		}
		return result;
	}
	public static Ident getNeighborIdent(Item neighborItem, String fieldName) {	
		Ident neighborItemIdent = neighborItem.getIdent();
		return getNeighborIdent(neighborItemIdent, fieldName);
	}
	public static String getString(Item parent, String fieldName, String defaultVal) {
		Ident pid = getNeighborIdent(parent, fieldName);
		return parent.getValString(pid, defaultVal);
	}
	public static Long getLong(Item parent,String fieldName, Long defaultVal) {
		Ident pid = getNeighborIdent(parent, fieldName);
		return parent.getValLong(pid, defaultVal);
	}
	public static Integer getInteger(Item parent,String fieldName, Integer defaultVal) {
		Long defLong = (defaultVal != null) ? new Long(defaultVal) : null;
		Long lv = getLong(parent, fieldName, defLong);
		return (lv != null) ? lv.intValue() : null;
	}	
	public static Double getDouble(Item parent, String fieldName, Double defaultVal) {
		Ident pid = getNeighborIdent(parent, fieldName);
		return parent.getValDouble(pid, defaultVal);
	}

	public static Set<Item> getLinkedItemSet(Item parent, String linkName) {
		Ident pid = getNeighborIdent(parent, linkName);
		return parent.getLinkedItemSet(pid);
	}
	public static int getLinkedItemCount(Item parent, String linkName) {
		Ident pid = getNeighborIdent(parent, linkName);
		return parent.getLinkedItemCount(pid);
	}
	public static Item getSingleLinkedItem(Item parent, String linkName) {
		Ident pid = getNeighborIdent(parent, linkName);
		return parent.getSingleLinkedItem(pid);
	}
	
	
}
