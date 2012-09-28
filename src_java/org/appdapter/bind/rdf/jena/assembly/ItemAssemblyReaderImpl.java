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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.appdapter.core.name.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.name.ModelIdent;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ItemAssemblyReaderImpl extends BasicDebugger implements ItemAssemblyReader {
	/**
	 * If optionalExplicitItem is not null, use it.  Otherwise, if compID is itself an Item, use it.  Otherwise, null.
	 * @param compID
	 * @param optionalExplicitItem
	 * @return 
	 */
	public Item chooseBestConfigItem(Ident compID, Item optionalExplicitItem) {
		Item infoSource = null;
		if (optionalExplicitItem != null) {
			infoSource = optionalExplicitItem;
		} else if (compID instanceof Item) {
			infoSource = (Item) compID;
		}
		return infoSource;
	} 
	@Override	public Ident getConfigPropertyIdent(Item infoSource, Ident compID, String fieldName) {
		Ident infoSourceID = infoSource.getIdent();
		logDebug("infoSourceID=" + infoSourceID + ", compID=" + compID);
		ModelIdent	someModelIdent = null;
		if (infoSourceID instanceof ModelIdent) {
			someModelIdent = (ModelIdent) infoSourceID;
		} else if (compID instanceof ModelIdent) {
			someModelIdent = (ModelIdent) compID;
		}
		Ident propertyIdent = null;
		if (someModelIdent != null) {
			propertyIdent = someModelIdent.getIdentInSameModel(fieldName);
		} else {
			logWarning("Cannot find a bootstrap ident to resolve fieldName: " + fieldName);
		}
		return propertyIdent;
	}

	// If we are using JenaResourceItems, then fieldName will be an absoluteURI.
	@Override
	public String readConfigValString(Ident compID, String fieldName, Item optionalItem, String defaultVal) {
		String resultVal = null;
		Item infoSource = chooseBestConfigItem(compID, optionalItem);
		// Typical output is
		// Resolved fieldName http://www.appdapter.org/schema/box#label to propertyIdent: JenaResourceItem[res=http://www.appdapter.org/schema/box#label]
		Ident propertyIdent = getConfigPropertyIdent(infoSource, compID, fieldName);
		logDebug("Resolved fieldName " + fieldName + " to propertyIdent: " + propertyIdent + ", to be fetched from source " + infoSource);
		if (propertyIdent != null) {
			resultVal = infoSource.getValString(propertyIdent, defaultVal);
		}
		return resultVal;
	}
	@Override
	public Long readConfigValLong(Ident compID, String fieldName, Item optionalItem, Long defaultVal) {
		Long resultVal = null;
		Item infoSource = chooseBestConfigItem(compID, optionalItem);
		// Typical output is
		// Resolved fieldName http://www.appdapter.org/schema/box#label to propertyIdent: JenaResourceItem[res=http://www.appdapter.org/schema/box#label]
		Ident propertyIdent = getConfigPropertyIdent(infoSource, compID, fieldName);
		logDebug("Resolved fieldName " + fieldName + " to propertyIdent: " + propertyIdent + ", to be fetched from source " + infoSource);
		if (propertyIdent != null) {
			resultVal = infoSource.getValLong(propertyIdent, defaultVal);
		}
		return resultVal;
	}

	@Override
	public Double readConfigValDouble(Ident compID, String fieldName, Item optionalItem, Double defaultVal) {
		Double resultVal = null;
		Item infoSource = chooseBestConfigItem(compID, optionalItem);
		// Typical output is
		// Resolved fieldName http://www.appdapter.org/schema/box#label to propertyIdent: JenaResourceItem[res=http://www.appdapter.org/schema/box#label]
		Ident propertyIdent = getConfigPropertyIdent(infoSource, compID, fieldName);
		logDebug("Resolved fieldName " + fieldName + " to propertyIdent: " + propertyIdent + ", to be fetched from source " + infoSource);
		if (propertyIdent != null) {
			resultVal = infoSource.getValDouble(propertyIdent, defaultVal);
		}
		return resultVal;
	}

	@Override
	public List<Item> readLinkedItemSeq(Item configItem, String collectionLinkName) {
		Ident linkNameID = getConfigPropertyIdent(configItem, configItem.getIdent(), collectionLinkName);
		List<Item> linkedItems = ((JenaResourceItem) configItem).getLinkedOrderedList(linkNameID);
		logDebug("Got linkedItem collection at [" + collectionLinkName + "=" + linkNameID + "] = " + linkedItems);	
		return linkedItems;
	}

	/**
	 * Note that the returned list is *not* yet sorted!  To be fixed.
	 * @param configItem
	 * @param linkName
	 * @param asmblr
	 * @param mode
	 * @param sortFieldNames
	 * @return 
	 */
	@Override
	public List<Object> findOrMakeLinkedObjects(Item configItem, String linkName, Assembler asmblr, Mode mode, List<Item.SortKey> sortFieldNames) {
		List<Object>	resultList = new ArrayList<Object>();
		Ident linkNameID = getConfigPropertyIdent(configItem, configItem.getIdent(), linkName);
		List<Item> linkedItems = configItem.getLinkedItemsSorted(linkNameID, sortFieldNames);
		resultList = resultListFromItems(linkedItems, asmblr, mode);
		return resultList;
	}

	@Override
	public List<Object> findOrMakeLinkedObjSeq(Item configItem, String collectionLinkName, Assembler asmblr, Mode mode) {
		List<Object>	resultList = new ArrayList<Object>();
		List<Item> linkedItems = readLinkedItemSeq(configItem, collectionLinkName);
		resultList = resultListFromItems(linkedItems, asmblr, mode);
		logDebug("Opened object collection : " + resultList);
		return resultList;
		/*
		Set<Item> linkedItemSet = configItem.getLinkedItemSet(linkNameID);
		logInfo("Found collection head set: " + linkedItemSet);
		if ((linkedItemSet != null) && (linkedItemSet.size() == 1)) {
			
		//	resultList = resultListFromItemList(linkedItems, assmblr, mode);		
		} else {
			logWarn("Expected one collection link at " + linkName + "=" + linkNameID + " but found " + linkedItemSet);
		}
		 * 
		 */		
	}
	/**
	 * For every linkedItem which is actually a JenaResourceItem, use the Jena assembler system to find/create
	 * the assembled object for that item.
	 * 
	 * @param linkedItems
	 * @param assmblr
	 * @param mode
	 * @return 
	 */

	@Override
	public List<Object> resultListFromItems(Collection<Item> linkedItems, Assembler assmblr, Mode mode) {
		List<Object>	resultList = new ArrayList<Object>();
		for (Item linkedItem : linkedItems) {
			if (linkedItem instanceof JenaResourceItem) {
				JenaResourceItem jri = (JenaResourceItem) linkedItem;
				// The assembler
				Object assembledObject = assmblr.open(assmblr, jri.getJenaResource(), mode);
				if (assembledObject != null) {
					resultList.add(assembledObject);
				} else {
					logWarning("Got null assembly result for item, ignoring: " + linkedItem);
				}
			} else {
				logWarning("Cannot assemble linked object from non-Jena item: " + linkedItem);
			}
		}
		return resultList;
	}	
}
