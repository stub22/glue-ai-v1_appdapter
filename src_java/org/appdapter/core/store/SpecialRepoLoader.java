package org.appdapter.core.store;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.rdf.model.Model;

/** 
  Repo loading in parallel
  
  LogicMoo
*/
public class SpecialRepoLoader extends BasicDebugger {
	public enum SheetLoadStatus {
		Pending, Loading, Loaded, Unloading, Unloaded, Cancelling, Cancelled, Error
	}

	public void logWarning(String msg) {
		getLogger().warn(msg);
	}

	ExecutorService executor = null;
	LinkedList<Task> tasks = new LinkedList<Task>();
	boolean lastJobSubmitted = false;
	Object synchronousAdderLock = new Object();
	boolean isSynchronous = true;
	int taskNum = 0;
	BasicRepoImpl loaderFor = null;
	String repoStr = "REPO";
	// sounds like a lot.. but it is over with quickly!
	int numThreads = 32;
	int howManyTasksBeforeStartingPool = 0;

	public SpecialRepoLoader(BasicRepoImpl repo) {
		loaderFor = repo;
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
	 Wait for the last load to happens
	*/
	public void waitUntilLastJobComplete() {

		int origTaskSize = 0;
		int newTaskSize = tasks.size();
		while (origTaskSize != newTaskSize) {
			for (Task task : ReflectUtils.copyOf(tasks)) {
				Task sheetLoadResult = task.get();
			}
			origTaskSize = newTaskSize;
			newTaskSize = tasks.size();
		}
		boolean waslastJobSubmitted;
		synchronized (synchronousAdderLock) {
			waslastJobSubmitted = lastJobSubmitted;
		}
		if (waslastJobSubmitted) {
			if (executor == null)
				return;
			//logWarning("Shutting down executor for " + repoStr);
			//executor.shutdown();
			//executor = null;
		} else {
			///logError("To Early to have called waitUntilLastJobComplete");
		}
	}

	synchronized public void addTask(String sheetNameURI, Runnable r) {
		Task task = new Task(sheetNameURI, r);
		synchronized (synchronousAdderLock) {
			if (isSynchronous || taskNum < howManyTasksBeforeStartingPool) {
				taskNum++;
				task.call();
				return;
			}

			if (executor == null) {
				lastJobSubmitted = false;
				logWarning("Creating executor for " + repoStr);
				executor = Executors.newFixedThreadPool(numThreads);
			}
			synchronized (tasks) {
				tasks.add(task);
			}
			task.future = executor.submit(task);
		}
	}

	/** Try to sheetLoad a URL. Return true only if successful. */
	public final class Task implements Callable<Task> {
		final String sheetName;
		SheetLoadStatus sheetLoadStatus = SheetLoadStatus.Unloaded;
		Future<Task> future;
		Runnable runIt;
		long start = -1, end = -1;
		Throwable lastException;

		@Override public String toString() {
			long soFar = (end == -1) ? System.currentTimeMillis() - start : end - start;
			return "TASK0: sheet=" + sheetName + " status=" + getLoadStatus() + " msecs=" + soFar + (lastException == null ? "" : " error=" + lastException);
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
			Model saveEventsTo = loaderFor.getEventsModel();
			Map eventProps = new HashMap();
			eventProps.put(RepoModelEvent.loadStatus, saveEventsTo.createResource("ccrt:" + newLoadStatus.toString()));
			eventProps.put(RepoModelEvent.timestamp, curMS);
			eventProps.put(RepoModelEvent.sheetName, sheetName);
			RepoModelEvent.createEvent(saveEventsTo, eventProps);
			logInfo(toString());
		}
	}
}