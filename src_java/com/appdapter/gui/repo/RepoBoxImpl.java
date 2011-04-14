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

package com.appdapter.gui.repo;

import arq.cmdline.ModTime;
import com.appdapter.gui.box.BoxImpl;
import com.appdapter.gui.box.Trigger;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.sparql.util.Timer;
import com.hp.hpl.jena.util.FileUtils;
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
	@Override public void uploadModelFile(String fileName, String graphName, boolean replace) {
		UploadTask ut = new UploadTask();
		ut.loadOne(myStore, fileName, graphName, replace);
	}
	public static class UploadTask {
		ModTime modTime = new ModTime();


		protected ModTime getModTime() {
			return modTime;
		}

		protected boolean isVerbose() {
			return true;
		}

		protected boolean isQuiet() {
			return false;
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

		private void loadOne(Store store, String filename, String graphName, boolean replace) {
			Monitor monitor = null;

			Model model = getModel(store, graphName);
			Graph graph = model.getGraph();

			if (isVerbose() && replace) {
				System.out.println("Emptying: " + filename);
			}
			if (replace) {
				model.removeAll();
			}

			if (isVerbose() || getModTime().timingEnabled()) {
				System.out.println("Start load: " + filename);
			}
			if (getModTime().timingEnabled()) {
				monitor = new Monitor(store.getLoader().getChunkSize(), isVerbose());
				graph.getEventManager().register(monitor);
			}

			// Crude but convenient
			if (filename.indexOf(':') == -1) {
				filename = "file:" + filename;
			}

			String lang = FileUtils.guessLang(filename);

			// Always time, only print if enabled.
			getModTime().startTimer();

			// Load here
			model.read(filename, lang);

			long timeMilli = getModTime().endTimer();

			if (monitor != null) {
				System.out.println("Added " + monitor.addCount + " triples");

				if (getModTime().timingEnabled() && !isQuiet()) {
					System.out.printf("Loaded in %.3f seconds [%d triples/s]\n",
							timeMilli / 1000.0, (1000 * monitor.addCount / timeMilli));
				}
				graph.getEventManager().unregister(monitor);
			}
		}

		static class Monitor implements GraphListener {

			int addNotePoint;
			long addCount = 0;
			int outputCount = 0;
			private Timer timer = null;
			private long lastTime = 0;
			private boolean displayMemory = false;

			Monitor(int addNotePoint, boolean displayMemory) {
				this.addNotePoint = addNotePoint;
				this.displayMemory = displayMemory;
				this.timer = new Timer();
				this.timer.startTimer();
			}

			public void notifyAddTriple(Graph g, Triple t) {
				addEvent(t);
			}

			public void notifyAddArray(Graph g, Triple[] triples) {
				for (Triple t : triples) {
					addEvent(t);
				}
			}

			@SuppressWarnings("unchecked")
			public void notifyAddList(Graph g, List triples) {
				notifyAddIterator(g, triples.iterator());
			}

			@SuppressWarnings("unchecked")
			public void notifyAddIterator(Graph g, Iterator it) {
				for (; it.hasNext();) {
					addEvent((Triple) it.next());
				}
			}

			public void notifyAddGraph(Graph g, Graph added) {
			}

			public void notifyDeleteTriple(Graph g, Triple t) {
			}

			@SuppressWarnings("unchecked")
			public void notifyDeleteList(Graph g, List L) {
			}

			public void notifyDeleteArray(Graph g, Triple[] triples) {
			}

			@SuppressWarnings("unchecked")
			public void notifyDeleteIterator(Graph g, Iterator it) {
			}

			public void notifyDeleteGraph(Graph g, Graph removed) {
			}

			public void notifyEvent(Graph source, Object value) {
			}

			private void addEvent(Triple t) {
				addCount++;
				if (addNotePoint > 0 && (addCount % addNotePoint) == 0) {
					outputCount++;
					long soFar = timer.readTimer();
					long thisTime = soFar - lastTime;

					// *1000L is milli to second conversion
					//   addNotePoint/ (thisTime/1000L)
					long tpsBatch = (addNotePoint * 1000L) / thisTime;
					long tpsAvg = (addCount * 1000L) / soFar;

					String msg = String.format("Add: %,d triples  (Batch: %d / Run: %d)", addCount, tpsBatch, tpsAvg);
					if (displayMemory) {
						long mem = Runtime.getRuntime().totalMemory();
						long free = Runtime.getRuntime().freeMemory();
						msg = msg + String.format("   [M:%,d/F:%,d]", mem, free);
					}
					System.out.println(msg);
					if (outputCount > 0 && (outputCount % 10) == 0) {
						System.out.printf("  Elapsed: %.1f seconds\n", (soFar / 1000F));
					}
					lastTime = soFar;
				}
			}
		}
	}
}
