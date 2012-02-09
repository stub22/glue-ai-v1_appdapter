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

package org.appdapter.gui.assembly;

import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.Item.SortKey;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.item.ModelIdent;
import org.appdapter.gui.box.KnownComponent;
import org.appdapter.gui.box.MutableKnownComponent;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 *
 * CC = "Component Class"
 */
public abstract class CachingComponentAssembler<MKC extends MutableKnownComponent> extends AssemblerBase {
	static Logger theLogger = LoggerFactory.getLogger(CachingComponentAssembler.class);
	private	Map<Ident, MKC> myCompCache = new HashMap<Ident, MKC>();

	protected abstract Class<MKC> decideComponentClass(Ident componentID, Item componentConfigItem);
	protected abstract void initExtendedFieldsAndLinks(MKC comp, Item configItem, Assembler asmblr, Mode mode);
	
	protected MKC getCachedComponent(Ident id) {
		return myCompCache.get(id);
	}
	protected void putCachedComponent(Ident id, MKC comp) {
		myCompCache.put(id, comp);
	}
	public static <KC extends KnownComponent> KC makeEmptyComponent(Class<KC> knownCompClass) {
		KC knownComp = null;
		try {
			knownComp = knownCompClass.newInstance();
		} catch (Throwable t) {
			theLogger.error("Problem instantiating empty component", t);
		}
		return knownComp;
	}
	public MKC fetchOrMakeComponent(Class<MKC> knownCompClass, Ident id, Assembler asmblr, Mode mode) {
		MKC knownComp = getCachedComponent(id);
		if (knownComp == null) {
			knownComp = makeEmptyComponent(knownCompClass);
			knownComp.setIdent(id);
			initFieldsAndLinks(knownComp, null, asmblr, mode);
			putCachedComponent(id, knownComp);
		}
		return knownComp;
	}
	private void initFieldsAndLinks(MKC comp, Item optionalExplicitItem, Assembler asmblr, Mode mode) {
		initLabelFields(comp, optionalExplicitItem);
		Item infoSource = chooseBestConfigItem(comp.getIdent(), optionalExplicitItem);
		if (infoSource != null) {
			initExtendedFieldsAndLinks(comp, infoSource, asmblr, mode);
		}
	}

	
	private void initLabelFields(MKC comp, Item optionalExplicitItem) {
		// Try to treat comp.ident as Jena resource and fetch label+desc automagically.
		Ident compID = comp.getIdent();
		String labelValString = readConfigValString(compID, AssemblyNames.P_label, optionalExplicitItem, "default-label");
		comp.setShortLabel(labelValString);
		String descValString = readConfigValString(compID, AssemblyNames.P_description, optionalExplicitItem, "default-desc");
		comp.setDescription(descValString);
	}
	private Item chooseBestConfigItem(Ident compID, Item optionalExplicitItem) {
		Item infoSource = null;
		if (optionalExplicitItem != null) {
			infoSource = optionalExplicitItem;
		} else if (compID instanceof Item) {
			infoSource = (Item) compID;
		}
		return infoSource;
	}
	protected Ident getConfigPropertyIdent(Item infoSource, Ident compID, String fieldName) {
		Ident infoSourceID = infoSource.getIdent();
		theLogger.info("infoSourceID=" + infoSourceID);
		theLogger.info("compID=" + compID);
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
			theLogger.warn("Cannot find a bootstrap ident to resolve fieldName: " + fieldName);
		}
		return propertyIdent;
	}
	// If we are using JenaResourceItems, then fieldName will be an absoluteURI.
	protected String readConfigValString(Ident compID, String fieldName, Item optionalItem, String defaultVal) {
		String resultVal = null;
		Item infoSource = chooseBestConfigItem(compID, optionalItem);
		theLogger.info("Best config item: " + infoSource);
		Ident propertyIdent = getConfigPropertyIdent(infoSource, compID, fieldName);
		theLogger.info("Resolved fieldName " + fieldName + " to propertyIdent: " + propertyIdent);
		if (propertyIdent != null) {
			resultVal = infoSource.getValString(propertyIdent, defaultVal);
		}
		return resultVal;
	}
	protected List<Object> findOrMakeLinkedObjects(Item configItem, String linkName, Assembler asmblr, Mode mode, List<SortKey> sortFieldNames) {
		List<Object>	resultList = new ArrayList<Object>();
		Ident linkNameID = getConfigPropertyIdent(configItem, configItem.getIdent(), linkName);
		List<Item> linkedItems = configItem.getSortedLinkedItemList(linkNameID, sortFieldNames);
		for (Item linkedItem : linkedItems) {
			if (linkedItem instanceof JenaResourceItem) {
				JenaResourceItem jri = (JenaResourceItem) linkedItem;
				Object assembledObject = asmblr.open(asmblr, jri.getJenaResource(), mode);
				if (assembledObject != null) {
					resultList.add(assembledObject);
				} else {
					theLogger.warn("Got null assembly result for item: " + linkedItem);
				}
			} else {
				theLogger.warn("Cannot assemble linked object from non-Jena item: " + linkedItem);
			}
		}
		return resultList;
	}
	@Override final public Object open(Assembler asmblr, Resource rsrc, Mode mode) {
		theLogger.info("Opening component at: " + rsrc);
		JenaResourceItem wrapperItem = new JenaResourceItem(rsrc);
		Class<MKC> componentClass = decideComponentClass(wrapperItem, wrapperItem);
		MKC comp = null;
		if (componentClass != null) {
			comp = fetchOrMakeComponent(componentClass, wrapperItem, asmblr, mode);
		}
		return comp;
	}

}
