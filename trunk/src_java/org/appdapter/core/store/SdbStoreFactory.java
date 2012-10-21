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

package org.appdapter.core.store;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.shared.Env;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.sparql.util.Timer;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.LocatorClassLoader;


import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DataSource;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatasetStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import com.hp.hpl.jena.sdb.shared.Env;
import com.hp.hpl.jena.util.FileManager;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.sdb.GraphUploadTask;

import org.appdapter.core.name.Ident;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class SdbStoreFactory {
	public static Logger getLogger() { 
		return LoggerFactory.getLogger(SdbStoreFactory.class);
	}
	/**
	 * Connect to Jena SDB Store using the supplied path, which is  The optional class for the class of 
	 * this repo is added to the list of classloaders known to the Jena file-resource loading
	 * system.   Any other required classloader (for your app's resource bundle) should have
	 * previously been added to the jena default FileManager using ______, or a FileManager should
	 * be explicitly set using _________.     SDBFactory supports several other config forms, including 
	 * any Jena model you have loaded from any source, so this API should be expanded with more equivalent
	 * methods, to allow user to supply any of those.
	 * 
	 * http://jena.apache.org/documentation/javadoc/sdb/com/hp/hpl/jena/sdb/SDBFactory.html
	 * 
	 * @param storeConfigPath - passed to SDBFactory.connectStore, is usually path to Turtle-format RDF-model file-resource on our classpath. 
	 * @param optLoaderToAdd - if nonnull, is passed to default-FileManager.addLocatorClassLoader.
	 * @return 
	 */

	public static Store connectSdbStoreFromResPath(String storeConfigPath, ClassLoader optLoaderToAdd) {
		getLogger().info("Connecting store using storeConfigPath[{}] and optionalCL[{}]", storeConfigPath, optLoaderToAdd);
		if (optLoaderToAdd != null) {
			FileManager fmgr = Env.fileManager();			
			fmgr.addLocatorClassLoader(optLoaderToAdd);
		}
		Store store = SDBFactory.connectStore(storeConfigPath);
		return store;
	}
}
