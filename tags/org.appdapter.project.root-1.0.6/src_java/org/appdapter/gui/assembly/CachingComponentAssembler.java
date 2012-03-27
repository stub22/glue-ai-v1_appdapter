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
import java.util.Collection;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Loggable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 *
 * CC = "Component Class"
 */
public abstract class CachingComponentAssembler<MKC extends MutableKnownComponent> extends AssemblerBase implements Loggable {
	static Logger theLogger = LoggerFactory.getLogger(CachingComponentAssembler.class);
	
	private	static Map<Class, ComponentCache> theCaches = new HashMap<Class, ComponentCache>();

	private	BasicDebugger myDebugger = new BasicDebugger();
	
	public CachingComponentAssembler() {
		myDebugger = new BasicDebugger();
	}
	
	
	protected ComponentCache<MKC>	getCache() { 
		Class	tc = getClass();
		// Not really type-safe, ugh.
		ComponentCache<MKC> cc = theCaches.get(tc);
		if (cc == null) { 
			cc = new ComponentCache<MKC>();
			theCaches.put(tc, cc);
		}
		return cc;
	}
	public static void clearCacheFor(Class c) {
		theCaches.put(c, null);
	}
	public static void clearAllCaches() { 
		theCaches = new HashMap<Class, ComponentCache>();
	}
	protected abstract Class<MKC> decideComponentClass(Ident componentID, Item componentConfigItem);
	protected abstract void initExtendedFieldsAndLinks(MKC comp, Item configItem, Assembler asmblr, Mode mode);
	
	public CachingComponentAssembler(Resource assemblerConfResource) {
		super();
		myDebugger.useLoggerForClass(getClass());
		// The AssemblerGroup re-does this on every call to open(), so we need to put our caches outside.
		logDebug("Constructing CCA " + toString() + " with config resource: " + assemblerConfResource);
		/*
		Exception e = new Exception();
		e.fillInStackTrace();
		e.printStackTrace();
		 * 
		 */
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
		ComponentCache<MKC> cc = getCache();
		MKC knownComp = cc.getCachedComponent(id);
		if (knownComp == null) {
			knownComp = makeEmptyComponent(knownCompClass);
			knownComp.setIdent(id);
			initFieldsAndLinks(knownComp, null, asmblr, mode);
			cc.putCachedComponent(id, knownComp);
		} else {
			logDebug("Got cache hit on " + knownComp);
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
	/**
	 * If optionalExplicitItem is not null, use it.  Otherwise, if compID is itself an Item, use it.  Otherwise, null.
	 * @param compID
	 * @param optionalExplicitItem
	 * @return 
	 */
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
	protected String readConfigValString(Ident compID, String fieldName, Item optionalItem, String defaultVal) {
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
	protected Long readConfigValLong(Ident compID, String fieldName, Item optionalItem, Long defaultVal) {
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
	protected Double readConfigValDouble(Ident compID, String fieldName, Item optionalItem, Double defaultVal) {
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
	protected List<Item> readLinkedItemSeq(Item configItem, String collectionLinkName) {
		Ident linkNameID = getConfigPropertyIdent(configItem, configItem.getIdent(), collectionLinkName);
		List<Item> linkedItems = ((JenaResourceItem) configItem).getLinkedOrderedList(linkNameID);
		logDebug("Got linkedItem collection at [" + collectionLinkName + "=" + linkNameID + "] = " + linkedItems);	
		return linkedItems;
	}
	protected List<Object> findOrMakeLinkedObjects(Item configItem, String linkName, Assembler asmblr, Mode mode, List<SortKey> sortFieldNames) {
		List<Object>	resultList = new ArrayList<Object>();
		Ident linkNameID = getConfigPropertyIdent(configItem, configItem.getIdent(), linkName);
		List<Item> linkedItems = configItem.getLinkedItemsSorted(linkNameID, sortFieldNames);
		resultList = resultListFromItems(linkedItems, asmblr, mode);
		return resultList;
	}
	protected List<Object> findOrMakeLinkedObjSeq(Item configItem, String collectionLinkName, Assembler asmblr, Mode mode) {
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
	protected List<Object> resultListFromItems(Collection<Item> linkedItems, Assembler assmblr, Mode mode) {
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
	}/**
	 * Our plugin for the Jena assembler will look in our cache first, making a new component only if requred.
	 * TODO:  Attempt conformance with the Jena Assembler "Mode" param.
	 * @param asmblr
	 * @param rsrc
	 * @param mode
	 * @return 
	 */
	@Override final public Object open(Assembler asmblr, Resource rsrc, Mode mode) {
		logDebug("Assembler[" + toString() + "] is opening component at: " + rsrc);
		JenaResourceItem wrapperItem = new JenaResourceItem(rsrc);
		Class<MKC> componentClass = decideComponentClass(wrapperItem, wrapperItem);
		MKC comp = null;
		if (componentClass != null) {
			comp = fetchOrMakeComponent(componentClass, wrapperItem, asmblr, mode);
		}
		return comp;
	}
	
	@Override public void logInfo(int importance, String msg) {
		myDebugger.logInfo(importance, msg);
	}
	@Override public void logInfo(String msg) {
		myDebugger.logInfo(msg);
	}
	@Override public void logError(String msg, Throwable t) {
		myDebugger.logError(msg, t);
	}
	@Override public void logError(String msg) {
		myDebugger.logError(msg);
	}
	@Override public void logWarning(String msg, Throwable t) {
		myDebugger.logWarning(msg, t);
	}
	@Override public void logWarning(String msg) {
		myDebugger.logWarning(msg);
	}
	public void logDebug(String msg) {
		myDebugger.logDebug(msg);
	}
}
