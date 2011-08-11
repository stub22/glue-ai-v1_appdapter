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

package org.appdapter.gui.repo;

import org.appdapter.binding.jena.sdb.GraphUploadTask;
import org.appdapter.binding.jena.model.GraphUploadMonitor;
import arq.cmdline.ModTime;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.Trigger;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RepoBoxImpl<TT extends Trigger<? extends RepoBoxImpl<TT>>> extends BoxImpl<TT> implements MutableRepoBox<TT> {
	static Logger theLogger = LoggerFactory.getLogger(RepoBoxImpl.class);
	public static Store		myStore;
	public static String	myStoreConfigPath;

	@Override public Store getStore() {
		return myStore;
	}

	@Override public void setStore(Store store) {
		myStore = store;
	}
	@Override public void mountStoreUsingFileConfig(String storeConfigPath) {
		theLogger.info("Mounting store using fileConfigPath[" + storeConfigPath + "]");
		FileManager fmgr = Env.fileManager();
		ClassLoader classLoader = getClass().getClassLoader();
		fmgr.addLocatorClassLoader(classLoader);
		Store store = SDBFactory.connectStore(storeConfigPath);
		setStore(store);
	}
	@Override public void formatStoreIfNeeded() {
		Store store = getStore();
		if (store == null) {
			throw new RuntimeException("Improperly asked to format store with no store open.");
		}
		try {
			boolean isFormatted = StoreUtils.isFormatted(store);
			theLogger.info("isFormatted returned  " + isFormatted);
			if (!isFormatted) {
				theLogger.info("Creating SDB tables in store: " + store);
				store.getTableFormatter().create();
			} else {
				theLogger.warn("Store " + store + " is already formatted, so ignoring init command.");
			}
		} catch (Throwable t) {
			theLogger.error("Problem in formatStoreIfNeeded", t);
		}
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
			theLogger.info("Found graph with URI: " + stat.graphURI + " and size: " + stat.statementCount);
			stats.add(stat);
		}
		return stats;
	}

	public String getUploadHomePath() {
		return ".";
	}
	@Override public void importGraphFromURL(String graphName, String sourceURL, boolean replaceTgtFlag) {
		GraphUploadTask ut = new GraphUploadTask();
		ut.loadOneGraphIntoStoreFromURL(myStore, graphName, sourceURL, replaceTgtFlag);
	}


}
