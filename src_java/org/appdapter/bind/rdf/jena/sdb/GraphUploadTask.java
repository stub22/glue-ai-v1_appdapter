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
package org.appdapter.bind.rdf.jena.sdb;

import arq.cmdline.ModTime;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.util.FileUtils;
import org.appdapter.bind.rdf.jena.model.GraphUploadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Includes code copied from Jena project file "sdbload.java".
 */
public class GraphUploadTask {
	
	static Logger theLogger = LoggerFactory.getLogger(GraphUploadTask.class);
	ModTime myModTime;
	boolean	myVerboseFlag, myQuietFlag;
	
	public GraphUploadTask() {
		this(true, true, false);
	}
	public GraphUploadTask(boolean timingEnabledFlag, boolean verboseFlag, boolean quietFlag) {
		myModTime = new ModTime();
		myModTime.setTimingEnabled(timingEnabledFlag);
		myVerboseFlag = verboseFlag;
		myQuietFlag = quietFlag;
	}

	protected ModTime getModTime() {
		return myModTime;
	}

	protected boolean isVerbose() {
		return myVerboseFlag;
	}

	protected boolean isQuiet() {
		return myQuietFlag;
	}

	public Graph getGraph(Store store, String graphName) {
		if (graphName == null) {
			return SDBFactory.connectDefaultGraph(store);
		} else {
			return SDBFactory.connectNamedGraph(store, graphName);
		}
	}

	public Model getModel(Store store, String graphName) {
		if (graphName == null) {
			return SDBFactory.connectDefaultModel(store);
		} else {
			return SDBFactory.connectNamedModel(store, graphName);
		}
	}
	// Code below here is mostly copied from sdbload.java

	public void loadOneGraphIntoStoreFromURL(Store store, String tgtGraphName, String sourceURL, boolean replaceTgtFlag) {
		GraphUploadMonitor monitor = null;

		Model tgtStoreModel = getModel(store, tgtGraphName);
		Graph graph = tgtStoreModel.getGraph();

		if (replaceTgtFlag) {
			if (isVerbose()) {
				theLogger.info("Emptying graph: " + tgtGraphName);
			}
			tgtStoreModel.removeAll();
		}
	// Crude but convenient
		if (sourceURL.indexOf(':') == -1) {
			sourceURL = "file:" + sourceURL;
		}

		if (isVerbose() || getModTime().timingEnabled()) {
			theLogger.info("Start loading from: " + sourceURL);
		}
		if (getModTime().timingEnabled()) {
			monitor = new GraphUploadMonitor(store.getLoader().getChunkSize(), isVerbose());
			graph.getEventManager().register(monitor);
		}
	
		String lang = FileUtils.guessLang(sourceURL);

		// Always time, only print if enabled.
		getModTime().startTimer();

		// Load here
		tgtStoreModel.read(sourceURL, lang);

		long timeMilli = getModTime().endTimer();

		long addedCnt = monitor.getAddCount();
		if (monitor != null) {
			theLogger.info("Added " +addedCnt + " triples");
		
			if (getModTime().timingEnabled() && !isQuiet()) {
				String outputMsg = String.format("Loaded in %.3f seconds [%d triples/s]\n", timeMilli / 1000.0, (1000 * addedCnt / timeMilli));
				theLogger.info(outputMsg);
			}
			graph.getEventManager().unregister(monitor);
		}
	}
}