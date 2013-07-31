package org.appdapter.gui.swing;

abstract class CantankerousJob implements Runnable {
	int skipped = 0;
	private Thread slow;
	private String jobname;
	private Object toStr;

	public CantankerousJob(String jobnam, Object name) {
		jobname = jobnam;
		this.toStr = name;
	}

	@Override public String toString() {
		return "CantankerousJob " + jobname + " for " + toStr;
	}

	public synchronized void attempt() {
		if (slow != null) {
			skipped++;
			return;
		}
		skipped++;
		slow = new Thread(toString()) {
			public void run() {
				while (skipped > 0) {
					skipped = 0;
					run();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
				if (slow == Thread.currentThread())
					slow = null;
			}

			@Override public String toString() {
				return CantankerousJob.this.toString();
			}
		};
		slow.start();

	}
}