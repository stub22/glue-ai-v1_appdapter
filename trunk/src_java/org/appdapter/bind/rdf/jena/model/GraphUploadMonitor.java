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
package org.appdapter.bind.rdf.jena.model;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.util.Timer;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Extracted and modified from Jena SDB's sdbload.java.
 */


public class GraphUploadMonitor implements GraphListener {

	static Logger theLogger = LoggerFactory.getLogger(GraphUploadMonitor.class);
	
	private Timer myTimer = null;
	private int myAddNotePoint;
	private boolean myDisplayMemoryFlag = false;
	
	private long myAddCount = 0;
	private int myOutputCount = 0;
	private long myLastEventTime = 0;


	public GraphUploadMonitor(int addNotePoint, boolean displayMemory) {
		myAddNotePoint = addNotePoint;
		myDisplayMemoryFlag = displayMemory;
		myTimer = new Timer();
		myTimer.startTimer();
	}
	
	public void notifyAddTriple(Graph g, Triple t) {
		addEvent(t);
	}

	public void notifyAddArray(Graph g, Triple[] triples) {
		for (Triple t : triples) {
			addEvent(t);
		}
	}
	
	@SuppressWarnings(value = "unchecked")
	public void notifyAddList(Graph g, List triples) {
		notifyAddIterator(g, triples.iterator());
	}

	@SuppressWarnings(value = "unchecked")
	public void notifyAddIterator(Graph g, Iterator it) {
		for (; it.hasNext();) {
			addEvent((Triple) it.next());
		}
	}

	public void notifyAddGraph(Graph g, Graph added) {
	}

	public void notifyDeleteTriple(Graph g, Triple t) {
	}

	@SuppressWarnings(value = "unchecked")
	public void notifyDeleteList(Graph g, List L) {
	}

	public void notifyDeleteArray(Graph g, Triple[] triples) {
	}

	@SuppressWarnings(value = "unchecked")
	public void notifyDeleteIterator(Graph g, Iterator it) {
	}

	public void notifyDeleteGraph(Graph g, Graph removed) {
	}

	public void notifyEvent(Graph source, Object value) {
	}

	private void addEvent(Triple t) {
		myAddCount++;
		if (myAddNotePoint > 0 && (myAddCount % myAddNotePoint) == 0) {
			myOutputCount++;
			long soFar = myTimer.readTimer();
			long thisTime = soFar - myLastEventTime;
			// *1000L is milli to second conversion
			//   addNotePoint/ (thisTime/1000L)
			long tpsBatch = (myAddNotePoint * 1000L) / thisTime;
			long tpsAvg = (myAddCount * 1000L) / soFar;
			String msg = String.format("Add: %,d triples  (Batch: %d / Run: %d)", myAddCount, tpsBatch, tpsAvg);
			if (myDisplayMemoryFlag) {
				long mem = Runtime.getRuntime().totalMemory();
				long free = Runtime.getRuntime().freeMemory();
				msg = msg + String.format("   [M:%,d/F:%,d]", mem, free);
			}
			System.out.println(msg);
			if (myOutputCount > 0 && (myOutputCount % 10) == 0) {
				System.out.printf("  Elapsed: %.1f seconds\n", soFar / 1000F);
			}
			myLastEventTime = soFar;
		}
	}

	public long getAddCount() {
		return myAddCount;
	}

	public int getAddNotePoint() {
		return myAddNotePoint;
	}

	public long getLastTime() {
		return myLastEventTime;
	}

	public int getOutputCount() {
		return myOutputCount;
	}
	
}