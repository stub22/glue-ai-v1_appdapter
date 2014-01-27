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

package org.appdapter.core.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

public class StatementSync {
	private ReentrantLock pauseLock = new ReentrantLock();

	static Logger theLogger = LoggerFactory.getLogger(RepoDatasetFactory.class);

	public class BulkStatement implements Runnable {
		public final Model dataModel;
		Model srcModel;
		public final WhatToDo toDo;

		public BulkStatement(Model srcModel, Model dataModel, WhatToDo todo) {
			this.srcModel = srcModel;
			this.dataModel = dataModel;
			toDo = todo;
		}

		@Override public void run() {
			statementsFrom(srcModel, dataModel, toDo);
		}
	}

	public class NotifyEvent implements Runnable {
		private final Model dataModel;
		private final Object event;
		Model srcModel;

		public NotifyEvent(Model srcModel, Object event, Model dataModel) {
			this.event = event;
			this.dataModel = dataModel;
			this.srcModel = srcModel;
		}

		@Override public void run() {
			StatementSync.this.notifyEventFrom(srcModel, dataModel, event);
		}
	}

	public class StatementEvent implements Runnable {
		private final Statement s;
		Model srcModel;
		WhatToDo toDo;

		public StatementEvent(Model srcModel, Statement s, WhatToDo tdDo) {
			this.s = s;
			this.srcModel = srcModel;
			toDo = tdDo;
		}

		@Override public void run() {
			statementFrom(srcModel, null, s, toDo);
		}
	}

	public class ChangeListener extends StatementListener implements ModelChangedListener {

		private boolean isDeaf;

		Model srcModel;

		public ChangeListener(Model src) {
			srcModel = src;
		}

		@Override public void addedStatement(final Statement s) {
			if (isDeaf())
				return;
			addTodo(s, new StatementEvent(srcModel, s, WhatToDo.Add));
		}

		@Override public void addedStatements(final Model dataModel) {
			if (isDeaf())
				return;
			addTodo(dataModel, new BulkStatement(srcModel, dataModel, WhatToDo.Add));
		}

		private void addTodo(Model dataModel, Runnable runnable) {
			StatementSync.this.addTodo(this, dataModel, runnable);
		}

		private void addTodo(Statement s, Runnable runnable) {
			StatementSync.this.addTodo(this, s, runnable);
		}

		public boolean isDeaf() {
			return isDeaf || StatementSync.this.isDisabled();
		}

		@Override public void notifyEvent(final Model dataModel, final Object event) {
			if (isDeaf())
				return;
			addTodo(dataModel, new NotifyEvent(srcModel, event, dataModel));
		}

		@Override public void removedStatement(final Statement s) {
			if (isDeaf())
				return;
			addTodo(s, new StatementEvent(srcModel, s, WhatToDo.Remove));
		}

		@Override public void removedStatements(final Model dataModel) {
			addTodo(dataModel, new BulkStatement(srcModel, dataModel, WhatToDo.Remove));
		}

		public void setDeaf(boolean isDeaf) {
			this.isDeaf = isDeaf;
		}
	}

	public static class Pair<A, B> {
		private static final Object NIL = null;

		public static <L, R> Pair<L, R> create(L x, R y) {
			return new Pair<L, R>(x, y);
		}

		final A a;
		final B b;

		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}

		public A car() {
			return a;
		}

		public B cdr() {
			return b;
		}

		private boolean equalObj(Object cdr1, Object cdr2) {

			return false;
		}

		@Override public boolean equals(Object other) {
			if (this == other)
				return true;

			// If it's a pair of a different <A,B> then .equals
			// Pair<A,B>(null,null) is equal to Pair<C,D>(null ,null)
			// Type erasure makes this hard to check otherwise.
			// Use class X extends Pair<A,B> and implement .equals to do
			// instanceof then call super.equals.

			if (!(other instanceof Pair<?, ?>))
				return false;
			Pair<?, ?> p2 = (Pair<?, ?>) other;
			return equalObj(car(), p2.car()) && equalObj(cdr(), p2.cdr());
		}

		public A getLeft() {
			return a;
		}

		public B getRight() {
			return b;
		}

		@Override public int hashCode() {
			return hashCodeObject(car()) ^ hashCodeObject(cdr()) << 1;
		}

		@Override public String toString() {
			StringBuffer sBuffer = new StringBuffer(20).append("(").append(str(a));
			Object cdrObject = b;
			while (cdrObject instanceof Pair) {
				Pair cPair = (Pair) b;
				sBuffer.append(" ").append(str(cPair.car()));
				cdrObject = cPair.cdr();
			}
			if (cdrObject == Pair.NIL)
				return sBuffer.append(")").toString();
			return sBuffer.append(" . ").append(str(cdrObject)).append(")").toString();
		}
	}

	abstract public class StatementOpListener {
		//public List<Statement> ignored = new ArrayList<Statement>();
		//public ConcurrentLinkedQueue<Statement> modelTodoList = new ConcurrentLinkedQueue();

		public void notifyEventFrom(StatementSync statementSync, Model srcModel, Model dataModel, Object event) {
			// TODO Auto-generated method stub

		}

		abstract public void statementFrom(StatementSync statementSync, Model srcModel, Model dataModel, Statement s, WhatToDo toDo);

		public void statementFroms(StatementSync statementSync, Model srcModel, Model dataModel, WhatToDo toDo) {
			for (Statement s : dataModel.listStatements().toList().toArray(new Statement[0])) {
				statementFrom(statementSync, srcModel, dataModel, s, toDo);
			}
		}

	}

	public class StatementOpListenerForModel extends StatementOpListener {
		Model destModel;
		protected boolean processesWholeModels;

		public StatementOpListenerForModel(Model target) {
			destModel = target;
		}

		@Override public void notifyEventFrom(StatementSync statementSync, Model srcModel, Model dataModel, Object event) {
			// TODO Auto-generated method stub
			super.notifyEventFrom(statementSync, srcModel, dataModel, event);
		}

		@Override public void statementFroms(StatementSync statementSync, Model srcModel, Model dataModel, WhatToDo toDo) {
			if (!processesWholeModels) {
				super.statementFroms(statementSync, srcModel, dataModel, toDo);
				return;
			}
			if (toDo == WhatToDo.Add) {
				destModel.add(dataModel);
			} else {
				destModel.remove(dataModel);
			}
		}

		@Override public void statementFrom(StatementSync statementSync, Model srcModel, Model dataModel, Statement s, WhatToDo toDo) {
			if (toDo == WhatToDo.Add) {
				destModel.add(s);
			} else {
				destModel.remove(s);
			}
		}
	}

	enum WhatToDo {
		Add, Remove;
	}

	static Map<Pair, StatementSync> syncPairs = new HashMap();

	static Collection<Lock> enterCriticalSections(boolean readLockRequested, Lock... tms) {
		HashSet<Lock> locks = new HashSet<Lock>();
		for (Lock l : tms) {
			if (locks.add(l)) {
				l.enterCriticalSection(readLockRequested);
			}
		}
		return locks;
	}

	public static StatementSync getStatementSyncerOfModels(Model m1, Model m2) {
		StatementSync statementSync = getModelsSyncer(m1, m2);
		return statementSync;
	}

	private static StatementSync getModelsSyncer(Model m1, Model m2) {
		Pair key = makeKey(m1, m2);
		synchronized (syncPairs) {
			StatementSync mcl = syncPairs.get(key);
			if (mcl == null) {
				Collection<Lock> locks = enterCriticalSections(false, m1, m2);
				theLogger.info("Making modelSync = " + key);
				try {
					Model memmodel = RepoDatasetFactory.createDefaultModelUnshared();
					memmodel.add(m1);
					m1.add(m2);
					m2.add(memmodel);
					memmodel.add(m2);
					mcl = new StatementSync(memmodel);
					mcl.addSourceModel_Int(m1);
					mcl.addSourceModel_Int(m2);
					syncPairs.put(key, mcl);
					mcl.addDestinationModel_Int(memmodel);
					mcl.addDestinationModel_Int(m1);
					mcl.addDestinationModel_Int(m2);
					mcl.setAsForegorund = true;

					return mcl;
				} finally {
					exitCriticalSections(locks);
				}
			}
			return mcl;
		}
	}

	public void addSourceModel(final Model src) {
		if (addSourceModel_Int(src)) {
			addTodo((ChangeListener) listenerMap.get(src), src, new BulkStatement(src, src, WhatToDo.Add));
		}
	}

	public void addDestinationModel(Model dest) {
		if (addDestinationModel_Int(dest)) {
			catchupDestination_Int(dest);
		}

	}

	/** HashCode - allow nulls */
	public static final int hashCodeObject(Object obj) {
		return hashCodeObject(obj, -4);
	}

	/** HashCode - allow nulls */
	public static final int hashCodeObject(Object obj, int nullHashCode) {
		if (obj == null)
			return nullHashCode;
		return obj.hashCode();
	}

	private static Pair makeKey(Model m1, Model m2) {
		if (System.identityHashCode(m1) > System.identityHashCode(m2)) {
			return new Pair(m2, m1);
		}
		return new Pair(m1, m2);
	}

	public static String str(Object x) {
		if (x == null)
			return "<null>";
		return x.toString();
	}

	public static StatementSync syncTwoModels(Model m1, Model m2) {
		StatementSync mcl = getStatementSyncerOfModels(m1, m2);
		mcl.completeSync();
		return mcl;
	}

	public static StatementSync resyncTwoModels(Model m1, Model m2) {
		StatementSync mcl = getStatementSyncerOfModels(m1, m2);
		mcl.resyncNow();
		return mcl;
	}

	static void exitCriticalSections(Collection<Lock> locks) {
		for (Lock l : locks) {
			l.leaveCriticalSection();
		}
	}

	Map<Object, ModelChangedListener> listenerMap = new HashMap();
	Set<Model> sourceModels = new HashSet(3);

	Set<Model> destModels = new HashSet(3);

	List<StatementSync.StatementOpListener> notifierList = new ArrayList();
	Map<Object, StatementSync.StatementOpListener> destMap = new HashMap();

	ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);

	void shutDown() {
		isSyncDisabled = true;
		if (shutDownRequested)
			return;
		shutDownRequested = true;
		// at some point at the end
		executor.shutdown();
	}

	private Model memoryCache;

	boolean shutDownRequested = false;
	boolean isSyncDisabled = false;
	private boolean setAsForegorund;

	public StatementSync(Model createDefaultModel) {
		this.memoryCache = createDefaultModel;
	}

	public boolean addSourceModel_Int(Model keyModel) {
		boolean isNew = false;
		synchronized (listenerMap) {
			ModelChangedListener mcl = listenerMap.get(keyModel);
			if (mcl == null) {
				Collection<Lock> locks = enterCriticalSection(false);
				mcl = new ChangeListener(keyModel);
				isNew = true;
				listenerMap.put(keyModel, mcl);
				sourceModels.add(keyModel);
				keyModel.register(mcl);
				exitCriticalSections(locks);
				pauseLock.unlock();
			}
		}
		return isNew;
	}

	public boolean addDestinationModel_Int(Model keyModel) {
		boolean isNew = false;
		synchronized (destMap) {
			StatementOpListener mcl = destMap.get(keyModel);
			if (mcl == null) {
				Collection<Lock> locks = enterCriticalSection(false);
				mcl = new StatementOpListenerForModel(keyModel);
				isNew = true;
				destMap.put(keyModel, mcl);
				sourceModels.add(keyModel);
				notifierList.add(mcl);
				exitCriticalSections(locks);
				pauseLock.unlock();
			}
		}
		return isNew;
	}

	public long catchupDestination_Int(Model destModel) {
		long size = destModel.size();
		destModel.add(memoryCache);
		return destModel.size() - size;
	}

	public void addTodo(ChangeListener changeListener, Model dataModel, Runnable runnable) {
		submit(runnable);
	}

	private void submit(Runnable runnable) {
		if (shutDownRequested)
			return;
		if (isForeground()) {
			runnable.run();
			return;
		}
		pauseLock.lock();
		executor.submit(runnable);
		pauseLock.unlock();
	}

	private boolean isForeground() {
		if (setAsForegorund)
			return true;
		if (executor == null || executor.isShutdown() || executor.isTerminated() || executor.isTerminating()) {
			throw new RuntimeException("!isForground");
		}
		return false;
	}

	public void addTodo(ChangeListener changeListener, Statement s, Runnable runnable) {
		submit(runnable);
	}

	private Collection<Lock> enterCriticalSection(boolean readLockRequested) {
		pauseLock.lock();

		HashSet<Lock> locks = null;
		synchronized (sourceModels) {
			locks = new HashSet<Lock>(sourceModels);
		}
		for (Lock l : locks) {
			l.enterCriticalSection(readLockRequested);
		}
		return locks;
	}

	public boolean isDisabled() {
		if (isSyncDisabled)
			return true;
		return shutDownRequested;
	}

	public void notifyEventFrom(Model srcModel, Model dataModel, Object event) {
		for (Iterator iterator = notifierList.iterator(); iterator.hasNext();) {
			StatementOpListener type = (StatementOpListener) iterator.next();
			type.notifyEventFrom(this, srcModel, dataModel, event);
		}
	}

	public void statementFrom(Model srcModel, Model dataModel, Statement s, WhatToDo toDo) {
		for (Iterator iterator = notifierList.iterator(); iterator.hasNext();) {
			StatementOpListener type = (StatementOpListener) iterator.next();
			type.statementFrom(this, srcModel, dataModel, s, toDo);
		}
	}

	public void statementsFrom(Model srcModel, Model dataModel, WhatToDo toDo) {
		for (Iterator iterator = notifierList.iterator(); iterator.hasNext();) {
			StatementOpListener type = (StatementOpListener) iterator.next();
			type.statementFroms(this, srcModel, dataModel, toDo);
		}
	}

	public void enableSync() {
		isSyncDisabled = false;

	}

	public void disableSync() {
		isSyncDisabled = true;

	}

	public void resyncNow() {
		Collection<Lock> locks = enterCriticalSection(false);
		boolean wasSyncDisabled = isSyncDisabled;
		try {
			isSyncDisabled = true;
			StatementSync mcl = this;
			Model memmodel = RepoDatasetFactory.createDefaultModelUnshared();
			synchronized (sourceModels) {
				for (Model m1 : sourceModels) {
					memmodel.add(m1);
				}
			}
			synchronized (destModels) {
				for (Model m1 : destModels) {
					m1.add(memmodel);
				}
			}
			this.memoryCache = memmodel;
		} finally {
			isSyncDisabled = wasSyncDisabled;
			exitCriticalSections(locks);
			pauseLock.unlock();
		}
	}

	public void completeSync() {
		catchup();
	}

	private void catchup() {
		pauseLock.lock();
		ScheduledThreadPoolExecutor exec = executor;
		executor = new ScheduledThreadPoolExecutor(1);
		pauseLock.unlock();

		exec.shutdown();
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}
	}
}
