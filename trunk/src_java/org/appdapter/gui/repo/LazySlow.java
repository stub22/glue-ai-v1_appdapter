package org.appdapter.gui.repo;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.appdapter.core.log.Debuggable;
import org.appdapter.core.store.Repo.Mutable;

public interface LazySlow<T> extends Future<T> {

	T getValue() throws ExecutionException;

	boolean isGetting();

	void startGetting();

	abstract public static class GetSet<T> implements LazySlow<T> {

		GetSet() {
			startGetting();
		}

		T m_val;
		boolean m_done;
		private boolean m_busy;
		private ExecutionException badness;
		private InterruptedException wasInterupted;

		final @Override public T getValue() throws ExecutionException {
			if (badness != null)
				throw badness;
			if (m_done)
				return m_val;
			synchronized (this) {
				safelyGet();
				if (badness != null)
					throw badness;
				return m_val;
			}
		}

		abstract protected T doGet() throws InterruptedException, ExecutionException, Throwable;

		@Override public boolean isGetting() {
			return m_busy;
		}

		Thread busy = null;
		private boolean m_cancelled;

		@Override public void startGetting() {
			if (busy != null) {
				return;
			}
			busy = new Thread() {
				@Override public void run() {
					GetSet.this.safelyGet();
				}
			};
			busy.start();
		}

		protected void safelyGet() {
			m_busy = true;
			try {
				doGet();
			} catch (InterruptedException e) {
				wasInterupted = e;
			} catch (ExecutionException e) {
				badness = e;
			} catch (Throwable e) {
				badness = new ExecutionException(e);
			}
			m_busy = false;
			m_done = true;
		}

		public void setReady(T repo) {
			m_val = repo;
			m_done = true;
			m_busy = false;
		}

		@Override public String toString() {
			return Debuggable.toInfoStringF(this);
		}

		@Override public boolean cancel(boolean mayInterruptIfRunning) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override public boolean isCancelled() {
			return m_cancelled;
		}

		@Override public boolean isDone() {
			return m_done;
		}

		@Override public T get() throws InterruptedException, ExecutionException {
			return getValue();
		}

		@Override public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			// TODO Auto-generated method stub
			return getValue();
		}
	}

	void setReady(T repo);

}
