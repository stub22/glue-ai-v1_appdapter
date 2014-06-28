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
package org.appdapter.core.loader;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.store.BasicRepoImpl;
import org.appdapter.core.store.RepoModelEvent;

import com.hp.hpl.jena.rdf.model.Model;
import org.appdapter.core.share.SharableRepoImpl;

/**
 * @author Logicmoo. <www.logicmoo.org>
 * 
 *         Repo loading in parallel Handling for a local *or* some 'remote'/'shared' model/dataset impl.
 * 
 */
public class SpecialRepoLoader extends BasicDebugger implements UncaughtExceptionHandler {
	public enum SheetLoadStatus {
		Pending, Loading, Loaded, Unloading, Unloaded, Cancelling, Cancelled, Error
	}
	private ExecutorService executor = null;
	private LinkedList<Task> tasks = new LinkedList<Task>();
	private boolean lastJobSubmitted = false;
	private Object synchronousAdderLock = new Object();
	private Object executorLock = new Object();
	private boolean isSynchronous = true;
	private int taskNum = 0;
	private LoadingRepoImpl myTargetRepoImpl = null;
	private String repoStr = "REPO";
	// sounds like a lot.. but it is over with quickly!
	private int numThreads = 32;
	private int howManyTasksBeforeStartingPool = 0;


	public SpecialRepoLoader(LoadingRepoImpl repo) {
		myTargetRepoImpl = repo;
	}
	
	public void addLoadTask(String s, Runnable task) {
		if (myTargetRepoImpl != null) {
			myTargetRepoImpl.addLoadTask(s, task);
		}
		else {
			addTask(s, task);
		}
	}

	public void setSynchronous(boolean isSync) {
		isSynchronous = isSync;
		if (isSync) {
			synchronized (synchronousAdderLock) {
				waitUntilLastJobComplete();
				if (executor != null) {
					executor.shutdown();
					executor = null;
				}
			}
		}
	}

	public void reset() {
		synchronized (synchronousAdderLock) {
			lastJobSubmitted = false;
		}
	}

	public void setLastJobSubmitted() {
		synchronized (synchronousAdderLock) {
			lastJobSubmitted = true;
		}
	}

	@Override public String toString() {
		if (false) {
			StringBuilder sbuf = new StringBuilder();
			int num = tasksWithsStatus(sbuf, true, SheetLoadStatus.Loaded);
			if (num > 0)
				return sbuf.toString();
		}
		return super.toString();
	}

	int tasksWithsStatus(StringBuilder sb, boolean neg, SheetLoadStatus... statuses) {
		int taskNum = 0;
		for (SheetLoadStatus status : statuses) {
			for (Task task : ReflectUtils.copyOf(tasks)) {
				if (task.getLoadStatus() != status) {
					if (!neg)
						continue;
				} else {
					if (neg)
						continue;
				}
				taskNum++;
				sb.append("" + taskNum + ": " + task.toString() + "\n");
			}
		}
		return taskNum;
	}

	/**
	 * Wait for the last load to happens
	 */
	public void waitUntilLastJobComplete() {

		int origTaskSize = 0;
		int newTaskSize = tasks.size();
		boolean isComplete = false;
		while (origTaskSize != newTaskSize || !isComplete) {
			isComplete = true;
			for (Task task : ReflectUtils.copyOf(tasks)) {
				// tast.get // makes the call happen
				Task sheetLoadResult = task.get();
				if (sheetLoadResult == null) {
					synchronized (tasks) {
						tasks.remove(task);
					}
					continue;
				}
				if (!sheetLoadResult.isComplete()) {
					isComplete = false;
				} else {
					// my report if the complition was bad?
					synchronized (tasks) {
						tasks.remove(task);
					}
				}
			}
			origTaskSize = newTaskSize;
			newTaskSize = tasks.size();
		}
		boolean waslastJobSubmitted;
		synchronized (synchronousAdderLock) {
			waslastJobSubmitted = lastJobSubmitted;
		}
		if (executor == null)
			return;
		synchronized (executorLock) {
			if (executor == null)
				return;
			logWarning("Shutting down executor for " + repoStr);
			executor.shutdown();
			executor = null;
			///logError("To Early to have called waitUntilLastJobComplete");
		}
	}

	public void addTask(String sheetNameURI, Runnable r) {
		if (lastJobSubmitted) {
			logWarning("AddTask " + sheetNameURI + " when lastJobSubmitted for " + repoStr);
		}
		Task task = new Task(sheetNameURI, r);
		//synchronized (synchronousAdderLock)
		{
			if (isSynchronous() || taskNum < howManyTasksBeforeStartingPool) {
				taskNum++;
				task.call();
				return;
			}
		}
		lastJobSubmitted = false;

		synchronized (executorLock) {
			if (executor == null) {
				logWarning("Creating executor for " + repoStr);
				executor = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
					@Override public Thread newThread(final Runnable r) {
						return new Thread("Worker " + ++workrNum + " for " + myTargetRepoImpl) {
							public void run() {
								r.run();
							}

							@Override public UncaughtExceptionHandler getUncaughtExceptionHandler() {
								return SpecialRepoLoader.this;
							}
						};

					}

				});
			}
		}
		synchronized (tasks) {
			tasks.add(task);
		}
		task.future = (Future<Task>) executor.submit((Runnable) task);
	}

	public int totalTasks = 0;
	public int workrNum = 0;
	private boolean alwaysSingleThreaded;

	/** Try to sheetLoad a URL. Return true only if successful. */
	public final class Task implements Callable<Task>, Runnable {
		final String sheetName;
		final int taskNum = totalTasks++;
		SheetLoadStatus sheetLoadStatus = SheetLoadStatus.Unloaded;
		Future<Task> future;
		Runnable runIt;
		long start = -1, end = -1;
		Throwable lastException;

		@Override public String toString() {
			long soFar = (end == -1) ? System.currentTimeMillis() - start : end - start;
			return "TASK-" + taskNum + ": sheet=" + sheetName + " status=" + getLoadStatus() + " msecs=" + soFar + (lastException == null ? "" : " error=" + lastException);
		}

		public boolean isComplete() {
			return end != -1;
		}

		public Task(String sheetNameURI, Runnable r) {
			this.sheetName = sheetNameURI;
			runIt = r;
			postLoadStatus(SheetLoadStatus.Pending, false);
		}

		void error(Throwable t) {
			lastException = t;
			logError(toString(), t);
		}

		@Override public void run() {
			call();
		}

		public Task get() {
			try {
				if (end != -1)
					return this;
				return future.get();
			} catch (Throwable e) {
				error(e);
				postLoadStatus(SheetLoadStatus.Error, true);
			}
			return this;
		}

		public Task call() {
			postLoadStatus(SheetLoadStatus.Loading, false);
			try {
				if (end != -1)
					return this;
				runIt.run();
				postLoadStatus(SheetLoadStatus.Loaded, true);
			} catch (Throwable e) {
				error(e);
				postLoadStatus(SheetLoadStatus.Error, true);
			}
			return this;
		}

		SheetLoadStatus getLoadStatus() {
			return sheetLoadStatus;
		}

		void postLoadStatus(SheetLoadStatus newLoadStatus, boolean isEnd) {
			if (newLoadStatus == this.sheetLoadStatus)
				return;
			long curMS = System.currentTimeMillis();
			if (isEnd) {
				this.end = curMS;
			} else {
				this.start = curMS;
			}
			this.sheetLoadStatus = newLoadStatus;
			Model saveEventsTo = myTargetRepoImpl.getEventsModel();
			Map eventProps = new HashMap();
			eventProps.put(RepoModelEvent.loadStatus, saveEventsTo.createResource("urn:ftd:cogchar.org:2012:runtime#" + newLoadStatus.toString()));
			eventProps.put(RepoModelEvent.timestamp, curMS);
			eventProps.put(RepoModelEvent.sheetName, sheetName);
			RepoModelEvent.createEvent(saveEventsTo, eventProps);
			String info = toString();
			Thread ct = Thread.currentThread();
			if (ct.getUncaughtExceptionHandler() instanceof SpecialRepoLoader) {
				ct.setName(info);
			}
			logInfo(info);
		}
	}

	@Override public void uncaughtException(Thread t, Throwable e) {
		logError(" uncaughtException on " + t, e);
		e.printStackTrace();
	}

	public void cancelAll() {
		executor.shutdown();
	}

	public int getRealTodo() {
		return tasks.size();

	}

	public boolean isSynchronous() {
		return isSynchronous || alwaysSingleThreaded;
	}

	public void addTaskFirst(String n, Runnable v) {
		ArrayList<? extends Runnable> drainedArrayList = new ArrayList();
		BlockingQueue queue = ((ThreadPoolExecutor) executor).getQueue();
		queue.drainTo(drainedArrayList);
		addTask(n, v);
		for (Runnable e : drainedArrayList) {
			queue.add(e);
		}

	}

	public void setSingleThreaded(boolean loadSingleThread) {
		alwaysSingleThreaded = loadSingleThread;

	}
	public void logWarning(String msg) {
		getLogger().warn(msg);
	}
}