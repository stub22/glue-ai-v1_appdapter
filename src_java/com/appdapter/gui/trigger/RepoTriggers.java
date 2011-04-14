/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.gui.trigger;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.TriggerImpl;
import com.appdapter.gui.repo.MutableRepoBox;
import com.appdapter.gui.repo.RepoBox;
import com.appdapter.gui.repo.RepoBox.GraphStat;
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

import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RepoTriggers {

	static Logger theLogger = LoggerFactory.getLogger(RepoTriggers.class);
	// public static Store theStore;
	public static String theStoreConfigPath = "testconf/store/appdtest_sdb_h2.ttl";
	public static String theSchemaTestPath = "testconf/owl/snazzy.owl";

	public static class OpenTrigger<MRB extends MutableRepoBox<TriggerImpl<MRB>>> extends  TriggerImpl<MRB>  {
		@Override public void fire(MRB targetBox) {
			targetBox.mountStoreUsingFileConfig(theStoreConfigPath);
		}
	}
	public static class InitTrigger<MRB extends MutableRepoBox<TriggerImpl<MRB>>> extends  TriggerImpl<MRB> {
		@Override public void fire(MRB targetBox) {
			targetBox.formatStoreIfNeeded();
		}
	}
	public static class DumpStatsTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends  TriggerImpl<RB> {
		public void fire(RB targetBox) {
			List<GraphStat> stats = targetBox.getGraphStats();
		}
	}
	public static class QueryTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends  TriggerImpl<RB>  {

		@Override public void fire(RB  targetBox) {
			Store store = targetBox.getStore();
			try {
				String queryText = "blah";
				String queryURL = "file:testconf/sparql/query_stuff.sparql";
				Query parsedQuery = QueryFactory.read(queryURL); //  wraps create(queryText);
				Dataset ds = DatasetStore.create(store);
				QueryExecution qe = QueryExecutionFactory.create(parsedQuery, ds);
				try {
					ResultSet rs = qe.execSelect();
					ResultSetRewindable rsr = ResultSetFactory.makeRewindable(rs);
					ResultSetFormatter.out(rsr);
					System.out.println("\ntriple results complete, starting XML\n===================================");
					rsr.reset();
					String resultXML = ResultSetFormatter.asXMLString(rsr);
					System.out.println(resultXML);
					 
				} finally {
					qe.close();
				}
			} catch (Throwable t) {
				theLogger.error("problem in QueryTrigger", t);
			}
		}
	}

	public static class UploadTrigger<MRB extends MutableRepoBox<TriggerImpl<MRB>>> extends  TriggerImpl<MRB>  {

		// ModGraph modGraph = new ModGraph();


		// Want contravariance?
		@Override public void fire(MRB targetBox) {
			try {
				String filePath = theSchemaTestPath;
				targetBox.uploadModelFile(filePath, "whoopee", true);
			} catch (Throwable t) {
				theLogger.error("problem in UploadTrigger", t);
			}
		}


	}
}
