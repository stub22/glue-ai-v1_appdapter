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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
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

import org.appdapter.core.log.BasicDebugger;

import org.appdapter.demo.DemoResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicStoredMutableRepoImpl extends BasicRepoImpl implements Repo.Stored, Repo.Mutable {

	// static Logger theLogger = LoggerFactory.getLogger(AppRepo.class);
	private String myStoreConfigPath;
	private Store	myStore;


	private BasicStoredMutableRepoImpl(String storeConfigPath) {
		myStoreConfigPath = storeConfigPath;
	}
	
	@Override public void mountStoreUsingFileConfig(String storeConfigPath) {
		logInfo("Mounting store using fileConfigPath[" + storeConfigPath + "]");
		FileManager fmgr = Env.fileManager();
		ClassLoader classLoader = getClass().getClassLoader();
		fmgr.addLocatorClassLoader(classLoader);
		Store store = SDBFactory.connectStore(storeConfigPath);
		setStore(store);
	}	

	private void open() {
		logInfo("Connecting to store using config from: " + myStoreConfigPath);
		mountStoreUsingFileConfig(myStoreConfigPath);
	}

	public static BasicStoredMutableRepoImpl openRepo(String storeConfigPath) {
		BasicStoredMutableRepoImpl repo = new BasicStoredMutableRepoImpl(storeConfigPath);
		repo.open();
		return repo;
	}

	@Override	public Store getStore() {
		/*
		* http://jena.apache.org/documentation/sdb/javaapi.html
		* A Store is lightweight and does not perform any database actions when created, so creating and releasing
		* them will not impact performance. Closing a store does not close the JDBC connection.
		*/		
		return myStore;
	}
	@Override public String getUploadHomePath() {
		return ".";
	}
	@Override public void setStore(Store store) {
		myStore = store;
	}
	
	@Override public List<GraphStat> getGraphStats() {
		List<GraphStat> stats = new ArrayList<GraphStat>();
		Store store = getStore();
		Iterator<Node> nodeIt = StoreUtils.storeGraphNames(store);
		while (nodeIt.hasNext()) {
			Node n = nodeIt.next();
			GraphStat stat = new GraphStat();
			stat.graphURI =  n.getURI();
			Model m = SDBFactory.connectNamedModel(store, stat.graphURI);
			stat.statementCount = m.size();
			logInfo("Found graph with URI: " + stat.graphURI + " and size: " + stat.statementCount);
			stats.add(stat);
		}
		return stats;
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

	@Override public void importGraphFromURL(String tgtGraphName, String sourceURL, boolean replaceTgtFlag) {
		GraphUploadTask ut = new GraphUploadTask();
		ut.loadOneGraphIntoStoreFromURL(myStore, tgtGraphName, sourceURL, replaceTgtFlag);
	}
	@Override public Dataset makeMainQueryDataset() {
		Store store = getStore();
		Dataset ds = DatasetStore.create(store);
		return ds;
	}


}
