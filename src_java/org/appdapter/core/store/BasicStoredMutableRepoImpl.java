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
package org.appdapter.core.store;

import org.appdapter.bind.rdf.jena.sdb.SdbStoreFactory;
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

import org.appdapter.demo.DemoResources;

/**
 * @author Stu B. <www.texpedient.com>
 *
 * This impl uses a Jena SDB "Store". 
 *
 * http://jena.apache.org/documentation/javadoc/sdb/com/hp/hpl/jena/sdb/Store.html
 */
public class BasicStoredMutableRepoImpl extends BasicRepoImpl implements Repo.Stored, Repo.Mutable {

	private Store myStore;



	/**
	 * 
	 * @param aStore 
	 */
	protected BasicStoredMutableRepoImpl(Store aStore) {
		myStore = aStore;
	}

	@Override public Store getStore() {
		/*
		 * http://jena.apache.org/documentation/sdb/javaapi.html A Store is lightweight and does not perform any
		 * database actions when created, so creating and releasing them will not impact performance. Closing a store
		 * does not close the JDBC connection.
		 */
		return myStore;
	}

	@Override public String getUploadHomePath() {
		return ".";
	}

	protected void setStore(Store store) {
		myStore = store;
	}

	@Override public void formatRepoIfNeeded() {
		Store store = getStore();
		if (store == null) {
			throw new RuntimeException("Improperly asked to format store with no store open.");
		}
		try {
			boolean isFormatted = StoreUtils.isFormatted(store);

			logInfo("isFormatted returned  " + isFormatted);
			if (!isFormatted) {
				logInfo("Creating SDB tables in store: " + store);
				myStore.getTableFormatter().create();
			} else {
				logWarning("Store " + store + " is already formatted, so ignoring init command.");
			}
		} catch (Throwable t) {
			logError("problem in formatIfNeeded", t);
		}
	}
	/**
	 * Uses a "GraphUploadTask" to read the contents of sourceURL into tgtGraph (named or default),
	 * with copious debug output.
	 * 
	 * @param tgtGraphName - resolved to a model using  SDBFactory.connectNamedModel.  If null, uses connectDefaultModel.
	 * @param sourceURL
	 * @param replaceTgtFlag - If true, model will first be cleared using tgtStoreModel.removeAll();
	 */
	@Override public void importGraphFromURL(String tgtGraphName, String sourceURL, boolean replaceTgtFlag) {
		// Copied settings from dflt constructor
		boolean timingEnabledFlag = true;
		boolean verboseFlag = true;
		boolean quietFlag = false;
		GraphUploadTask ut = new GraphUploadTask(timingEnabledFlag, verboseFlag, quietFlag);
		ut.loadOneGraphIntoStoreFromURL(myStore, tgtGraphName, sourceURL, replaceTgtFlag);
	}

	@Override public Dataset makeMainQueryDataset() {
		Store store = getStore();
		Dataset ds = DatasetStore.create(store);
		return ds;
	}
	/*
	 * Does not work with Jena 2.6.4. May work in some later (Apache-era) Jena versions.
	 *
	 * protected DataSource getMainMutableDataSource() { Dataset mds = getMainQueryDataset(); return (DataSource) mds;
	}
	 */

	protected Model connectNamedModel(Ident modelID) {
		return SDBFactory.connectNamedModel(getStore(), modelID.getAbsUriString());
	}

	@Override public void addNamedModel(Ident modelID, Model srcModel) {
		// DataSource mutableDataSrc = (DataSource) getMainMutableDataSource();
		// Newer versions of Jena API include this method in the Dataset interface.
		// mutableDataSrc.addNamedModel(modelID.getAbsUriString(), model);
		Model connModel = connectNamedModel(modelID);
		/*
		 * logInfo("Connected named model: " + connModel); Graph g = connModel.getGraph(); logInfo("Graph = " + g);
		 * logInfo("BulkUpdateHandler = " + g.getBulkUpdateHandler());
		 *
		 */
		connModel.add(srcModel);
	}

	@Override public void replaceNamedModel(Ident modelID, Model srcModel) {
		// DataSource mutableDataSrc = (DataSource) getMainMutableDataSource();
		// Newer versions of Jena API include this method in the Dataset interface.
		// mutableDataSrc.replaceNamedModel(modelID.getAbsUriString(), model);
		Model connModel = connectNamedModel(modelID);
		connModel.removeAll();
		connModel.add(srcModel);
	}
	
	
	/**
	 * For unit testing, this static convenience method constructs the "basic" version of Store repo.  
	 * See details in SdbStoreFactory.  
	 * For real application use cases, we instead usually  go through the FancyRepoFactory object defined in Scala Packages of 
	 * this proj.
	 * @param storeConfigPath
	 * @param optCL
	 * @return 
	 */
	public static BasicStoredMutableRepoImpl openBasicRepoFromConfigPath(String storeConfigPath, ClassLoader optCL) {
		Store s = SdbStoreFactory.connectSdbStoreFromResPath(storeConfigPath, optCL);
		BasicStoredMutableRepoImpl repo = new BasicStoredMutableRepoImpl(s);
		return repo;
	}	
	
	
	/**
	 * Old sample store-iterator code, replaced by Dataset-level impls. in BasicRepoImpl.getGraphStats.
	 * Iterating the store's graphs directly, rather than thru Dataset. 
	 * Will give absolutely equivalent results?
	 * 
	 * @param store
	 * @return 
	 */
	protected List<GraphStat> unusedDirectGraphStatFetcher(Store store) {
		List<GraphStat> stats = new ArrayList<GraphStat>();
		Iterator<Node> nodeIt = StoreUtils.storeGraphNames(store);
		while (nodeIt.hasNext()) {
			Node n = nodeIt.next();
			GraphStat stat = new GraphStat();
			stat.graphURI = n.getURI();
			Model m = SDBFactory.connectNamedModel(store, stat.graphURI);
			stat.statementCount = m.size();
			logInfo("Found graph with URI: " + stat.graphURI + " and size: " + stat.statementCount);
			stats.add(stat);
		}
		return stats;
	}
	
	
}
