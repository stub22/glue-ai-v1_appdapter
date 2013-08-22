package org.appdapter.gui.browse;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.swing.CantankerousJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblerCacheGrabber extends BasicDebugger implements AnyOper.Singleton, AnyOper.Autoload {

	static Logger theLogger = LoggerFactory.getLogger(AssemblerCacheGrabber.class);
	static int instances = 0;

	public AssemblerCacheGrabber() {
		instances++;
		theLogger.warn("Made this " + instances);
	}

	@UISalient(ResultIsSingleton = true)//
	public Map<Class, ComponentCache> getCacheMap() {
		return AssemblerUtils.getComponentCacheMap(AssemblerUtils.getDefaultSession());
	}

	public static String anyToString(Object any) {
		return "" + any;
	}

	@UISalient public void loadAssemblerClasses() {
		setLongRunner("loadAssemblerClasses", new Runnable() {
			@Override public void run() {
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				Map<Class, ComponentCache> map = getCacheMap();
				final Object[] clzes;
				synchronized (map) {
					clzes = map.keySet().toArray();
				}
				for (Object c : clzes) {
					bp.findOrCreateBox(c);
				}
			}
		});
	}

	@UISalient() public void loadAssemblerInstances() {
		setLongRunner("loadAssemblerInstances", new Runnable() {

			@Override public void run() {
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				Map<Class, ComponentCache> cmap = getCacheMap();
				final Object[] clzes;
				synchronized (cmap) {
					clzes = cmap.values().toArray();
				}
				for (Object c : clzes) {
					Map<Ident, Object> map = (Map<Ident, Object>) ((ComponentCache) c).getCompCache();
					synchronized (map) {
						for (Map.Entry<Ident, Object> me : map.entrySet()) {
							Utility.recordCreated(bp, me.getKey(), me.getValue());
						}
					}
				}
			}
		});
	}

	@UISalient() public void loadAddedBoxes() {
		setLongRunner("loadAddedBoxes", new Runnable() {

			@Override public void run() {
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				Map<Class, ComponentCache> cmap = getCacheMap();
				final Object[] clzes;
				synchronized (cmap) {
					clzes = cmap.values().toArray();
				}
				for (Object c : clzes) {
					Map<Object, BT> map = Utility.allBoxes;
					List<Entry<Object, BT>> es;
					synchronized (map) {
						es = ReflectUtils.copyOf(map.entrySet());
					}
					for (Map.Entry<Object, BT> me : es) {
						Object obj = me.getKey();
						Utility.asWrapped(obj);
					}
				}

			}
		});
	}

	public Map<String, CantankerousJob> longThreads = new HashMap();
	private Object longThreadSync = longThreads;

	synchronized void setLongRunner(String named, final Runnable longRunner) {

		synchronized (longThreadSync) {
			CantankerousJob cj = longThreads.get(named);
			if (cj == null) {
				cj = new CantankerousJob(named, this) {

					@Override public void run() {
						longRunner.run();

					}
				};
				longThreads.put(named, cj);
			}
			cj.attempt();
		}

	}

	@UISalient public void loadBasicDebuggerInstances() {
		setLongRunner("loadBasicDebuggerInstances", new Runnable() {
			@Override public void run() {
				Collection all = Debuggable.allObjectsForDebug;
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				final Collection allCopy;
				synchronized (all) {
					allCopy = new LinkedList(all);
				}
				for (Object o : allCopy) {
					if (o.getClass() == BasicDebugger.class)
						continue;
					bp.findOrCreateBox(o);
				}
			}
		});
	}

}
