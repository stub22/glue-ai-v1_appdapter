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

import java.util.Collection;
import java.util.List;

import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Original thought was that these link+field property names are abstract names that needn't necessarily be "absolute URIs".
 * However, in practice, as of 2013, when we are using Jena to do the value lookups, then these Strings are always treated
 * as absolute URIs.   To improve this situation, the customer application code (that is using this assembly reader)
 * should make use of existing utilitiesfor constructing *explicit* property Idents, and use those instead of relying 
 * on these methods to *implicitly* resolve the link/field property names.
 */

public interface ItemAssemblyReader {

	List<Object> findOrMakeLinkedObjSeq(Item configItem, String collectionLinkName_absUri, Assembler asmblr, Mode mode);

	List<Object> findOrMakeLinkedObjects(Item configItem, String linkName_absUri, Assembler asmblr, Mode mode, List<Item.SortKey> sortFieldNames);

	Ident getConfigPropertyIdent(Item infoSource, Ident compID, String fieldName_absUri);

	Double readConfigValDouble(Ident compID, String fieldName_absUri, Item optionalItem, Double defaultVal);

	Long readConfigValLong(Ident compID, String fieldName_absUri, Item optionalItem, Long defaultVal);

	String readConfigValString(Ident compID, String fieldName_absUri, Item optionalItem, String defaultVal);

	List<Item> readLinkedItemSeq(Item configItem, String collectionLinkName_absUri);

	/**
	 * For every linkedItem which is actually a JenaResourceItem, use the Jena assembler system to find/create
	 * the assembled object for that item.
	 *
	 */
	List<Object> resultListFromItems(Collection<Item> linkedItems, Assembler assmblr, Mode mode);
}
