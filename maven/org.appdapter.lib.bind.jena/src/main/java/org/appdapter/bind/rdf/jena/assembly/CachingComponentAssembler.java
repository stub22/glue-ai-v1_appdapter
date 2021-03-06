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

package org.appdapter.bind.rdf.jena.assembly;

import java.util.Map;

import org.appdapter.core.component.ComponentAssemblyNames;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.name.Ident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.appdapter.bind.rdf.jena.model.SerialJenaResItem;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
//import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
//import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReaderImpl;

/**
 *
 * @author Stu B. <www.texpedient.com>
 *
 * CC = "Component Class"
 * 
 * This class maintains a horrible lynchpin for all our Jena Assembler-based features
 * (which are completely optional):  A static map of ComponentCaches, keyed by component class.
 * We cannot rely on instance data very easily in Assemblers, because Jena can construct them at will.
 * 
 * Using this map, we ensure that clients get the same object handles, every time they use
 * a cooperating Assembler to look up a component for a particular URI.
 */
public abstract class CachingComponentAssembler<MKC extends MutableKnownComponent> extends AssemblerBase {
	private Logger myLogger = LoggerFactory.getLogger(getClass());
	private static Logger theBackupLogger = LoggerFactory.getLogger(CachingComponentAssembler.class);

	final public static String DEFAULT_LABEL = "default-label-";
	public static String DEFAULT_DESC = "default-desc";
	//private	static Map<Class, ComponentCache> theCachesByAssmblrSubclass = new HashMap<Class, ComponentCache>();

	// private	BasicDebugger myDebugger = new BasicDebugger(getClass());

	protected ItemAssemblyReader myReader = new ItemAssemblyReaderImpl();

	protected Logger getLogger() {
		return myLogger;
	}

	public CachingComponentAssembler() {
		// myDebugger = new BasicDebugger();
	}

	public ItemAssemblyReader getReader() {
		return myReader;
	}

	protected AssemblerSession getSession() {
		return AssemblerUtils.getDefaultSession();
	}

	protected ComponentCache<MKC> getCache() {
		Class tc = getClass();
		// Not really type-safe, ugh.
		Map<Class, ComponentCache> map = AssemblerUtils.getComponentCacheMap(getSession());
		ComponentCache<MKC> cc = map.get(tc);
		if (cc == null) {
			cc = new ComponentCache<MKC>();
			map.put(tc, cc);
		}
		return cc;
	}

	/* This will be removed
	public static void clearCacheForAssemblerSubclass(Class c) {
		//theCachesByAssmblrSubclass.put(c, null);
	}
	public static void clearAllSubclassCaches() { 
		//theCachesByAssmblrSubclass = new HashMap<Class, ComponentCache>();
	}//*/

	protected abstract Class<MKC> decideComponentClass(Ident componentID, Item componentConfigItem);

	protected abstract void initExtendedFieldsAndLinks(MKC comp, Item configItem, Assembler asmblr, Mode mode);

	public CachingComponentAssembler(Resource assemblerConfResource) {
		super();
		// The AssemblerGroup re-does this on every call to open(), so we need to put our caches outside.
		getLogger().debug("Constructing CCA {} with config resource: {} ", this, assemblerConfResource);
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
			theBackupLogger.error("Problem instantiating empty component of class {}", knownCompClass, t);
		}
		return knownComp;
	}

	public MKC fetchOrMakeComponent(Ident compID, Item confItem, Assembler asmblr, Mode mode) {
		ComponentCache<MKC> cc = getCache();
		MKC knownComp = cc.getCachedComponent(compID);
		if (knownComp == null) {
			Class<MKC> knownCompClass = decideComponentClass(compID, confItem);
			knownComp = makeEmptyComponent(knownCompClass);
			knownComp.setIdent(compID);
			initFieldsAndLinks(knownComp, null, asmblr, mode);
			cc.putCachedComponent(compID, knownComp);
		} else {
			getLogger().debug("Got cache hit on {} ", knownComp);
		}
		return knownComp;
	}

	private void initFieldsAndLinks(MKC comp, Item optionalExplicitItem, Assembler asmblr, Mode mode) {
		initLabelFields(comp, optionalExplicitItem);
		Item infoSource = ((ItemAssemblyReaderImpl) myReader).chooseBestConfigItem(comp.getIdent(), optionalExplicitItem);
		if (infoSource != null) {
			initExtendedFieldsAndLinks(comp, infoSource, asmblr, mode);
		}
	}

	private void initLabelFields(MKC comp, Item optionalExplicitItem) {
		// Try to treat comp.ident as Jena resource and fetch label+desc automagically.
		Ident compID = comp.getIdent();
		String labelValString = myReader.readConfigValString(compID, ComponentAssemblyNames.P_label, optionalExplicitItem, DEFAULT_LABEL);
		comp.setShortLabel(labelValString);
		String descValString = myReader.readConfigValString(compID, ComponentAssemblyNames.P_description, optionalExplicitItem, DEFAULT_DESC);
		comp.setDescription(descValString);
	}

	/**
		 * Our plugin for the Jena assembler will look in our cache first, making a new component only if requred.
		 * TODO:  Attempt conformance with the Jena Assembler "Mode" param.
		 * @param asmblr
		 * @param rsrc
		 * @param mode
		 * @return 
		 */
	final public Object open(Assembler asmblr, Resource rsrc, Mode mode) {
		getLogger().debug("Assembler[{}] is opening component at: {}", this, rsrc);
		JenaResourceItem wrapperItem = new SerialJenaResItem(rsrc);
		//Class<MKC> componentClass = decideComponentClass(wrapperItem, wrapperItem);
		//MKC comp = null;
		//if (componentClass != null) {
		MKC comp = fetchOrMakeComponent(wrapperItem, wrapperItem, asmblr, mode);
		//}
		return comp;
	}

}
