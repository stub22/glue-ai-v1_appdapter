/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appdapter.gui.trigger;

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
 * @author winston
 */
public class RepoTriggers {

	static Logger theLogger = LoggerFactory.getLogger(RepoTriggers.class);
	// public static Store theStore;
	public static String theStoreConfigPath = "testconf/store/appdtest_sdb_h2.ttl";
	public static String theSchemaTestPath = "testconf/owl/snazzy.owl";

	public static class OpenTrigger extends TriggerImpl<MutableRepoBox<OpenTrigger>>  {
		@Override public void fire(MutableRepoBox targetBox) {
			targetBox.mountStoreUsingFileConfig(theStoreConfigPath);
		}
	}
	public static class InitTrigger extends TriggerImpl<MutableRepoBox<InitTrigger>> {
		@Override public void fire(MutableRepoBox targetBox) {
			targetBox.formatStoreIfNeeded();
		}
	}
	public static class DumpStatsTrigger extends TriggerImpl<RepoBox<DumpStatsTrigger>> {
		public void fire(RepoBox targetBox) {
			List<GraphStat> stats = targetBox.getGraphStats();
		}
	}
	public static class QueryTrigger extends TriggerImpl<RepoBox<QueryTrigger>> {

		@Override public void fire(RepoBox targetBox) {
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

	public static class UploadTrigger extends TriggerImpl<MutableRepoBox<UploadTrigger>> {

		// ModGraph modGraph = new ModGraph();


		// Want contravariance?
		@Override public void fire(MutableRepoBox<UploadTrigger> targetBox) {
			try {
				String filePath = theSchemaTestPath;
				targetBox.uploadModelFile(filePath, "whoopee", true);
			} catch (Throwable t) {
				theLogger.error("problem in UploadTrigger", t);
			}
		}


	}
}
