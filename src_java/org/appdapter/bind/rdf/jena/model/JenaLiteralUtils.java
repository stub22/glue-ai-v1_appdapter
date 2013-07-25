/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.component.IdentToObjectListener;
import org.appdapter.core.convert.Converter;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.FrontsNode;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class JenaLiteralUtils {
	public static Logger theLogger = LoggerFactory.getLogger(JenaLiteralUtils.class);

	public static Object findComponent(Ident ident, Class mustBe) {
		Map<Class, ComponentCache> cmap = getCacheMap();
		if (mustBe == Object.class || mustBe == null)
			return anyOfAssemblerInstances(ident);

		Object[] keys;
		synchronized (cmap) {
			keys = cmap.keySet().toArray();
		}
		for (Object k : keys) {
			Class ck = (Class) k;
			if (!mustBe.isAssignableFrom(ck))
				continue;
			Map<Ident, Object> map = null;
			synchronized (cmap) {
				map = cmap.get(ck).getCompCache();
			}
			synchronized (map) {
				return map.get(ident);
			}
		}
		return null;
	}

	public static Map<Class, ComponentCache> getCacheMap() {
		return AssemblerUtils.getComponentCacheMap(AssemblerUtils.getDefaultSession());
	}

	public static Map<Class, ComponentCache> getObjectCacheMap() {
		return AssemblerUtils.getComponentCacheMap(AssemblerUtils.getDefaultSession());
	}

	public static Object anyOfAssemblerInstances(Ident ident) {
		Map<Class, ComponentCache> cmap = getCacheMap();
		Object[] clzes;
		synchronized (cmap) {
			clzes = cmap.values().toArray();
		}

		for (Object c : clzes) {
			Map<Ident, Object> map = (Map<Ident, Object>) ((ComponentCache) c).getCompCache();
			synchronized (map) {
				Object c1 = map.get(ident);
				if (c1 != null)
					return c1;
			}
		}
		return null;
	}

	public static <T> T convertRDFNodeStatic(Object e0, Class<T> type) throws Throwable {
		if (!(e0 instanceof RDFNode)) {
			throw new NoSuchConversionException(e0, type);
		}

		RDFNode e = (RDFNode) e0;
		type = ReflectUtils.nonPrimitiveTypeFor(type);
		if (Number.class.isAssignableFrom(type)) {
			return (T) e.asLiteral().getValue();
		}
		if (Boolean.class.isAssignableFrom(type)) {
			return (T) e.asLiteral().getValue();
		}
		if (RDFNode.class.isAssignableFrom(type)) {
			Class<RDFNode> rtype = (Class<RDFNode>) type;

			if (e.canAs(rtype))
				return (T) e.as(rtype);
		}
		if (Ident.class.isAssignableFrom(type)) {
			return (T) new FreeIdent(e.asNode().getURI());
		}
		if (String.class.isAssignableFrom(type)) {
			try {
				return (T) e.asLiteral().getValue();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		Object eval = e;
		if (e.isLiteral()) {
			eval = e.asLiteral().getValue();
		} else if (e.isURIResource()) {
			String uri = e.asNode().getURI();
			eval = findComponent(new FreeIdent(uri), type);
			if (eval == null) {

			}
		}

		if (!type.isInstance(eval)) {
			throw new NoSuchConversionException(e0, type);
		}
		return (T) eval;
	}

	public static boolean isMatchAny(Ident val) {
		if (val == null)
			return true;
		return false;
	}

	public static boolean isTypeMatch(Ident hasThingType, Ident targetThingTypeID) {
		if (isMatchAny(hasThingType) || isMatchAny(targetThingTypeID))
			return true;
		if (hasThingType.equals(targetThingTypeID)) {
			return true;
		}
		return false;
	}

	public static boolean isIndividualMatch(Ident hasThing, Ident targetThingID) {
		if (isMatchAny(hasThing) || isMatchAny(targetThingID))
			return true;
		if (hasThing.equals(targetThingID)) {
			return true;
		}
		return false;
	}

	public static boolean isMatch(Object mustBe, Object raw) {
		if (mustBe == null)
			return false;
		if (raw == null)
			return false;
		return mustBe.equals(raw);
	}

	public static LinkedList<IdentToObjectListener> identListeners = new LinkedList<IdentToObjectListener>();

	public static void addIdListener(IdentToObjectListener listener) {
		synchronized (identListeners) {
			identListeners.remove(listener);
			identListeners.add(0, listener);
		}
	}

	public static void removeIdListener(IdentToObjectListener listener) {
		synchronized (identListeners) {
			identListeners.remove(listener);
		}
	}

	public static void onSetIdent(Ident id, Object value) {

		synchronized (identListeners) {
			for (IdentToObjectListener listener : identListeners) {
				listener.registerURI(id, value);
			}
		}
	}

	public static void onRemoveIdent(Ident id, Object value) {
		synchronized (identListeners) {
			for (IdentToObjectListener listener : identListeners) {
				listener.deregisterURI(id, value);
			}
		}
	}

}
