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

import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.api.trigger.Trigger;

import com.hp.hpl.jena.query.Query;


import java.util.List;
import org.appdapter.core.store.BasicStoredMutableRepoImpl;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.Repo.GraphStat;

import com.hp.hpl.jena.query.ResultSet;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;


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
		myRepo = BasicStoredMutableRepoImpl.openRepo(configPath);
	}
	@Override public void formatStoreIfNeeded() {
		myRepo.formatRepoIfNeeded();
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
		Query parsedQuery = JenaArqQueryFuncs.parseQueryURL(queryURL);
		String xmlOut = myRepo.processQuery(parsedQuery, null, new JenaArqResultSetProcessor<String>() {
			@Override public String processResultSet(ResultSet rset) {
				return JenaArqQueryFuncs.dumpResultSetToXML(rset);
			}
		});
		return xmlOut;

	}


}
