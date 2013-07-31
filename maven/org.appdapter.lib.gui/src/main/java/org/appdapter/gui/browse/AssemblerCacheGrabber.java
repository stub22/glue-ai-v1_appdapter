package org.appdapter.gui.browse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.NamedObjectCollection;

public class AssemblerCacheGrabber extends BasicDebugger implements AnyOper.Singleton {

	public Map<Class, ComponentCache> getCacheMap() {
		return AssemblerUtils.getComponentCacheMap(AssemblerUtils.getDefaultSession());
	}

	public static String anyToString(Object any) {
		return "" + any;
	}

	@Override public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	public boolean longThreadQuit = false;

	@UISalient public void loadAssemblerClasses() {
		final NamedObjectCollection bp = Utility.getTreeBoxCollection();
		Map<Class, ComponentCache> map = getCacheMap();
		final Object[] clzes;
		synchronized (map) {
			clzes = map.keySet().toArray();
		}
		setLongRunner(new Runnable() {
			@Override public void run() {
				for (Object c : clzes) {
					bp.findOrCreateBox(c);
					if (longThreadQuit) {
						longThreadQuit = false;
						return;
					}
				}
			}
		});
	}

	@UISalient() public void loadAssemblerInstances() {
		final NamedObjectCollection bp = Utility.getTreeBoxCollection();
		Map<Class, ComponentCache> cmap = getCacheMap();
		final Object[] clzes;
		synchronized (cmap) {
			clzes = cmap.values().toArray();
		}

		setLongRunner(new Runnable() {

			@Override public void run() {
				for (Object c : clzes) {
					Map<Ident, Object> map = (Map<Ident, Object>) ((ComponentCache) c).getCompCache();
					synchronized (map) {
						for (Map.Entry<Ident, Object> me : map.entrySet()) {
							if (longThreadQuit) {
								longThreadQuit = false;
								return;
							}
							Utility.recordCreated(bp, me.getKey(), me.getValue());
						}
					}
				}
			}
		});
	}

	private Object longThreadSync = this;
	public Thread longThread;

	synchronized void setLongRunner(final Runnable longRunner) {

		synchronized (longThreadSync) {
			if (this.longThread != null) {
				longThreadQuit = true;
				try {
					longThread.join();
				} catch (InterruptedException e) {
				}
			}
			longThread = new Thread() {
				public void destroy() {
					longThreadQuit = true;
				}

				public void run() {
					longRunner.run();
					//synchronized (longThreadSync) 
					{
						if (longThread == Thread.currentThread()) {
							longThread = null;
						}
					}
				};
			};
			longThreadQuit = false;
			longThread.start();
		}

	}

	@UISalient public void loadBasicDebuggerInstances() {
		Collection all = Debuggable.allObjectsForDebug;
		final NamedObjectCollection bp = Utility.getTreeBoxCollection();
		final Collection allCopy;
		synchronized (all) {
			allCopy = new LinkedList(all);
		}
		setLongRunner(new Runnable() {

			@Override public void run() {
				for (Object o : allCopy) {
					if (o.getClass() == BasicDebugger.class)
						continue;
					if (longThreadQuit) {
						longThreadQuit = false;
						return;
					}
					bp.findOrCreateBox(o);
				}
			}
		});
	}
}
