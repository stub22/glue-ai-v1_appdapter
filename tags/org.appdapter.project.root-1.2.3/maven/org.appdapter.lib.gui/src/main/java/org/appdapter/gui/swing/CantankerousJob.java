package org.appdapter.gui.swing;

import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.browse.Utility;

abstract public class CantankerousJob implements Runnable {
	int skipped = 0;
	private Thread slow;
	private String jobname;
	private Object toStr;
	final Object lock = new Object();
	boolean isUIThreadTask = false;

	public Object getLock() {
		return lock;
	}

	public CantankerousJob(String jobnam, Object name, boolean inUIThread) {
		jobname = jobnam;
		this.toStr = name;
		isUIThreadTask = inUIThread;
	}

	@Override public String toString() {
		return "CantankerousJob " + jobname + " for " + toStr;
	}

	public void attempt() {
		synchronized (lock) {
			// lock just long enough to create the thread and start it
			attemptImpl();
		}
	}

	protected void attemptImpl() {

		if (slow != null) {
			skipped++;
			return;
		}

		skipped++;
		slow = new Thread(toString()) {
			@Override
			public void run() {
				while (skipped > 0) {
					skipped = 0;
					try {
						if (isUIThreadTask) {
							Utility.invokeAndWait(CantankerousJob.this);
						} else {
							CantankerousJob.this.run();
						}
					} catch (Throwable t) {
						t.printStackTrace();
						Debuggable.printStackTrace(t);
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
						}
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
				synchronized (lock) {
					if (slow == Thread.currentThread())
						slow = null;
				}

			}

			@Override public String toString() {
				return CantankerousJob.this.toString();
			}
		};
		slow.start();

	}
}