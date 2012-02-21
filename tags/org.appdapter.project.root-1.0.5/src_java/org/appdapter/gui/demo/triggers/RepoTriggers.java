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

package org.appdapter.gui.demo.triggers;

import org.appdapter.bind.rdf.jena.model.AssemblerUtils;
import org.appdapter.gui.box.TriggerImpl;
import org.appdapter.gui.repo.MutableRepoBox;
import org.appdapter.gui.repo.RepoBox;
import org.appdapter.gui.repo.RepoBox.GraphStat;
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
import java.net.URL;
import java.util.List;
import org.appdapter.demo.DemoResources;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RepoTriggers {

	static Logger theLogger = LoggerFactory.getLogger(RepoTriggers.class);

	public static class OpenTrigger<MRB extends MutableRepoBox<TriggerImpl<MRB>>> extends  TriggerImpl<MRB>  {
		@Override public void fire(MRB targetBox) {
			String storeConfigResolvedPath = DemoResources.STORE_CONFIG_PATH; // DemoResources.resolveResourcePathToURL_WhichJenaCantUseInCaseOfJarFileRes(DemoResources.STORE_CONFIG_PATH);
			// Model data = FileManager.get().loadModel(dataPath.toString());
			targetBox.mountStoreUsingFileConfig(storeConfigResolvedPath);
		}
	}
	public static class InitTrigger<MRB extends MutableRepoBox<TriggerImpl<MRB>>> extends  TriggerImpl<MRB> {
		@Override public void fire(MRB targetBox) {
			targetBox.formatStoreIfNeeded();
		}
	}
	public static class DumpStatsTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends  TriggerImpl<RB> {
		@Override public void fire(RB targetBox) {
			List<GraphStat> stats = targetBox.getGraphStats();
		}
	}
	public static class QueryTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends  TriggerImpl<RB>  {

		@Override public void fire(RB  targetBox) {
			Store store = targetBox.getStore();
			try {
				theLogger.info("Registering classLoader with JenaFM");
				AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(getClass().getClassLoader());
				String unusedInlineQueryText = "blah";
				String resolvedQueryURL = DemoResources.QUERY_PATH; // DemoResources.resolveResourcePathToURL_WhichJenaCantUseInCaseOfJarFileRes(DemoResources.QUERY_PATH);
				Query parsedQuery = QueryFactory.read(resolvedQueryURL); //  wraps create(queryText);
				Dataset ds = DatasetStore.create(store);
				QueryExecution qe = QueryExecutionFactory.create(parsedQuery, ds);
				try {
					ResultSet rs = qe.execSelect();
					ResultSetRewindable rsr = ResultSetFactory.makeRewindable(rs);
					ResultSetFormatter.out(rsr);
					theLogger.info("\ntriple results complete, starting XML\n===================================");
					rsr.reset();
					String resultXML = ResultSetFormatter.asXMLString(rsr);
					theLogger.info(resultXML);
					 
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
				String tgtGraphName = "yowza";
				
				// TODO - check on DemoResources.OPTIONAL_ABSOLUTE_ROOT_PATH
				String absolutePathInNeigborClassSpace = "/" + DemoResources.DATA_PATH;
				String dataSourceURL = 
				DemoResources.makeURLforClassNeighborResPath_JenaFMCantUseButModelReaderCan(getClass(), absolutePathInNeigborClassSpace);
					
				targetBox.importGraphFromURL(tgtGraphName, dataSourceURL, true);
			} catch (Throwable t) {
				theLogger.error("problem in UploadTrigger", t);
			}
		}


	}
}
