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

package org.appdapter.bind.rdf.jena.assembly;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.util.Collection;
import java.util.List;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;

/**
 * @author Stu B. <www.texpedient.com>
 */

public interface ItemAssemblyReader {

	List<Object> findOrMakeLinkedObjSeq(Item configItem, String collectionLinkName, Assembler asmblr, Mode mode);

	List<Object> findOrMakeLinkedObjects(Item configItem, String linkName, Assembler asmblr, Mode mode, List<Item.SortKey> sortFieldNames);

	Ident getConfigPropertyIdent(Item infoSource, Ident compID, String fieldName);

	Double readConfigValDouble(Ident compID, String fieldName, Item optionalItem, Double defaultVal);

	Long readConfigValLong(Ident compID, String fieldName, Item optionalItem, Long defaultVal);

	String readConfigValString(Ident compID, String fieldName, Item optionalItem, String defaultVal);

	List<Item> readLinkedItemSeq(Item configItem, String collectionLinkName);

	/**
	 * For every linkedItem which is actually a JenaResourceItem, use the Jena assembler system to find/create
	 * the assembled object for that item.
	 *
	 */
	List<Object> resultListFromItems(Collection<Item> linkedItems, Assembler assmblr, Mode mode);
}
