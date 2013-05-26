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

import java.util.List;

import org.appdapter.api.trigger.Trigger;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;
import org.appdapter.core.store.BasicStoredMutableRepoImpl;
import org.appdapter.core.store.MutableRepoBox;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.Repo.GraphStat;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class RepoBoxImpl<TT extends Trigger<? extends RepoBoxImpl<TT>>> extends ScreenBoxImpl<TT> implements MutableRepoBox<TT> {
	static Logger theLogger = LoggerFactory.getLogger(RepoBoxImpl.class);
	private Repo.Mutable myRepo;

	@Override final public Object getObject() {
		return getRepo();
	}

	@Override public void setObject(Object obj) {
		// TODO Auto-generated method stub
		setRepo((Repo.Mutable) obj);
	}

	// Store		myStore;
	// public static String	myStoreConfigPath;

	@Override public Repo getRepo() {
		return getMRepo();
	}

	public Repo.Mutable getMRepo() {
		return myRepo;
	}

	public void setRepo(Repo.Mutable repo) {
		myRepo = repo;
	}

	@Override public void mount(String configPath) {
		// This bonehead static method call does not allow us to construct a fancier subtype of BasicStoredMutableRepoImpl.
		Repo.Mutable myRepo = BasicStoredMutableRepoImpl.openBasicRepoFromConfigPath(configPath, getClass().getClassLoader());
		setRepo(myRepo);
	}

	@Override public void formatStoreIfNeeded() {
		Repo.Mutable myRepo = getMRepo();
		myRepo.formatRepoIfNeeded();
	}

	@Override public List<GraphStat> getAllGraphStats() {
		Repo.Mutable myRepo = getMRepo();
		return myRepo.getGraphStats();
	}

	@Override public String getUploadHomePath() {
		Repo.Mutable myRepo = getMRepo();
		return myRepo.getUploadHomePath();
	}

	@Override public void importGraphFromURL(String graphName, String sourceURL, boolean replaceTgtFlag) {
		Repo.Mutable myRepo = getMRepo();
		myRepo.importGraphFromURL(graphName, sourceURL, replaceTgtFlag);
	}

	@Override public String processQueryAtUrlAndProduceXml(String queryURL, ClassLoader optResourceCL) {
		Query parsedQuery = JenaArqQueryFuncs.parseQueryURL(queryURL, optResourceCL);
		Repo.Mutable myRepo = getMRepo();
		String xmlOut = myRepo.processQuery(parsedQuery, null, new JenaArqResultSetProcessor<String>() {
			@Override public String processResultSet(ResultSet rset) {
				return JenaArqQueryFuncs.dumpResultSetToXML(rset);
			}
		});
		return xmlOut;

	}

}
