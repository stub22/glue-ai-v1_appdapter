package org.appdapter.gui.browse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.NamedObjectCollection;

public class AssemberCacheGrabber extends BasicDebugger {
	public Map<Class, ComponentCache> getCacheMap() {
		return AssemblerUtils.getComponentCacheMap(AssemblerUtils.getDefaultSession());
	}

	@UISalient public void loadAssemblerClasses() {
		NamedObjectCollection bp = Utility.getTreeBoxCollection();
		Map<Class, ComponentCache> map = getCacheMap();
		Object[] clzes;
		synchronized (map) {
			clzes = map.keySet().toArray();
		}
		for (Object c : clzes) {
			bp.findOrCreateBox(c);
		}
	}

	@UISalient public void loadAssemblerInstances() {
		NamedObjectCollection bp = Utility.getTreeBoxCollection();
		Map<Class, ComponentCache> cmap = getCacheMap();
		Object[] clzes;
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

	@UISalient public void loadBasicDebuggerInstances() {
		Collection all = Debuggable.allObjectsForDebug;
		NamedObjectCollection bp = Utility.getTreeBoxCollection();
		synchronized (all) {
			all = new LinkedList(all);
		}
		for (Object o : all) {
			bp.findOrCreateBox(o);
		}
	}
}
