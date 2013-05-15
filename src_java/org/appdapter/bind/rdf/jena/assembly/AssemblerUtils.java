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

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.AssemblerHelp;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.core.component.ComponentCache;

import org.appdapter.core.log.BasicDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class AssemblerUtils {
	static Logger theLogger = LoggerFactory.getLogger(AssemblerUtils.class);
	static BasicDebugger theDbg = new BasicDebugger();
    
    private static AssemblerSession theDefaultSession = new AssemblerSession();
    private static Map<AssemblerSession,Map<Class, ComponentCache>> theSessionComponentCacheMap = new HashMap<AssemblerSession, Map<Class, ComponentCache>>();
    
    /**
     * Clears all of the objects of a given type from the object cache.
     * 
     * @param c The Type of cache that is too be cleared
     * @param s The session that owns the caches
     */
    public static void clearCacheForAssemblerSubclassForSession(Class c, AssemblerSession s) {
        Map<Class, ComponentCache> map = AssemblerUtils.getComponentCacheMap(s);
        map.put(c, null);
	}
    
    /**
     * Clears all of the caches in a given session.
     * 
     * @param s The session that owns the caches
     */
	public static void clearAllSubclassCachesForSession(AssemblerSession s) { 
        Map<Class, ComponentCache> map = AssemblerUtils.getComponentCacheMap(s);
        map.clear();
	}
    
    
    public static AssemblerSession getDefaultSession(){
        return theDefaultSession;
    }
    
    public static Map<Class, ComponentCache> getComponentCacheMap(AssemblerSession session){
        Map<Class, ComponentCache> map = theSessionComponentCacheMap.get(session);
        if(map == null){
            map = new HashMap<Class, ComponentCache>();
            theSessionComponentCacheMap.put(session, map);
        }
        return map;
    }

	public static Set<Object>	buildAllRootsInModel(Assembler jenaAssembler, Model jenaModel, Mode jenaAssemblyMode) {
		Set<Object> results = new HashSet<Object>();
		Set<Resource> aroots = AssemblerHelp.findAssemblerRoots(jenaModel);
		theLogger.info("Found " + aroots.size() + " assembler-roots in model");
		for (Resource aroot : aroots) {
			Object result = jenaAssembler.open(jenaAssembler, aroot, jenaAssemblyMode);
			results.add(result);
		}
		return results;
	}
	public static Set<Object>	buildAllRootsInModel(Model jenaModel) {
		return buildAllRootsInModel(Assembler.general, jenaModel, Mode.DEFAULT);
	}
	public static Set<Object> buildAllObjectsInRdfFile(String rdfURL) {
		Model	loadedModel =  FileManager.get().loadModel(rdfURL);
		Set<Object> results = buildAllRootsInModel(loadedModel);
		return results;
	}

	public static Set<Object> buildObjSetFromPath(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		if (optResourceClassLoader != null) {
			theDbg.logDebug("Ensuring registration of classLoader: " + optResourceClassLoader);
			JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(optResourceClassLoader);
		}
		theDbg.logInfo("Loading triples from flex-path: " + rdfConfigFlexPath);
		Set<Object> loadedStuff = buildAllObjectsInRdfFile(rdfConfigFlexPath);
		return loadedStuff;
	}
	public static <T> T readOneConfigObjFromPath(Class<T> configType, String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		Set<Object> loadedStuff = buildObjSetFromPath (rdfConfigFlexPath, optResourceClassLoader);
		int objCount = loadedStuff.size();
		if (objCount != 1) {
			throw new RuntimeException("Expected one config thing but got " + objCount + " from path[" + rdfConfigFlexPath + "]");
		}
		Object singleConfigObj =  loadedStuff.toArray()[0];
		Class objClass = singleConfigObj.getClass();
		if (configType.isAssignableFrom(objClass)) {
			return (T) singleConfigObj;
		} else {
			throw new RuntimeException("Expected config object type " + configType + " but got " + objClass);
		}
	}	
}
