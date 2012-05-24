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

import org.appdapter.bind.rdf.jena.sdb.GraphUploadTask;
import org.appdapter.bind.rdf.jena.model.GraphUploadMonitor;
import arq.cmdline.ModTime;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.api.trigger.Trigger;
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

import com.hp.hpl.jena.query.Query;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.appdapter.core.store.BasicRepoImpl;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.Repo.GraphStat;
import org.appdapter.core.store.Repo.ResultSetProc;

import com.hp.hpl.jena.query.ResultSet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RepoBoxImpl<TT extends Trigger<? extends RepoBoxImpl<TT>>> extends ScreenBoxImpl<TT> implements MutableRepoBox<TT> {
	static Logger theLogger = LoggerFactory.getLogger(RepoBoxImpl.class);
	public static Repo.Mutable		myRepo;
	
	// Store		myStore;
	// public static String	myStoreConfigPath;

	@Override public Repo getRepo() {
		return myRepo;
	}

	@Override public void mount(String configPath) {
		myRepo = BasicRepoImpl.openRepo(configPath);
	}
	@Override public void formatStoreIfNeeded() {
		myRepo.formatStoreIfNeeded();
	}
	@Override public List<GraphStat> getAllGraphStats() {
		return myRepo.getGraphStats();
	}
	@Override public String getUploadHomePath() {
		return myRepo.getUploadHomePath();
	}
	@Override public void importGraphFromURL(String graphName, String sourceURL, boolean replaceTgtFlag) {
		myRepo.importGraphFromURL(graphName, sourceURL, replaceTgtFlag);
	}

	@Override public String processQueryAtUrlAndProduceXml(String queryURL) {
		Query parsedQuery = myRepo.parseQueryURL(queryURL);
		String xmlOut = myRepo.processQuery(parsedQuery, new ResultSetProc<String>() {
			@Override public String processResultSet(ResultSet rset) {
				return BasicRepoImpl.dumpResultSetToXML(rset);
			}
		});
		return xmlOut;

	}


}
