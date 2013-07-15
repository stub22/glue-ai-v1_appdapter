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
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class JenaLiteralUtils {
	public static Logger theLogger = LoggerFactory.getLogger(JenaLiteralUtils.class);

	public static <T> T convertList(List<RDFNode> e, Class<T> type) throws Throwable {
		T result = null;
		if (e == null || e.size() == 0)
			return result;
		int len = e.size();
		if (type.isArray()) {
			Class ctype = type.getComponentType();
			result = (T) Array.newInstance(ctype, e.size());
			for (int i = 0; i < len; i++) {
				Array.set(result, i, convert(e.get(i), ctype));
			}
			return result;
		}
		if (!Collection.class.isAssignableFrom(type)) {
			if (len != 1) {
				theLogger.warn("Can only use one result from " + e);
			}
			return convert(e.get(0), type);
		}
		Class<T> concrete = null;
		if (Modifier.isAbstract(type.getModifiers()) || type.isInterface()) {
			for (Class c : new Class[] { ArrayList.class, Stack.class, HashSet.class }) {
				if (type.isAssignableFrom(c)) {
					concrete = c;
				}
			}
			if (concrete == null) {
				throw new ClassCastException("Cannot create abstract " + type + " from " + e);
			}
		} else {
			concrete = type;
		}
		Collection cresult = (Collection) (result = concrete.newInstance());
		Iterator<RDFNode> its = e.listIterator();
		Class<Object> compType = Object.class;
		while (its.hasNext()) {
			cresult.add(convert(its.next(), compType));
		}
		return result;
	}

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

	static public Object anyOfAssemblerInstances(Ident ident) {
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

	public static <T> T convert(RDFNode e, Class<T> type) throws Throwable {
		type = nonPrimitiveTypeFor(type);
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
			return (T) e.asLiteral().getValue();
		}
		Object eval = e;
		if (e.isLiteral()) {
			eval = e.asLiteral().getValue();
		} else if (e.isURIResource()) {
			String uri = e.asNode().getURI();
			eval = findComponent(new FreeIdent(uri), type);
		}

		if (!type.isInstance(eval)) {
			throw new ClassCastException("Cannot create " + type + " from " + e);
		}
		return (T) eval;
	}

	public static Class nonPrimitiveTypeFor(Class wrapper) {
		if (!wrapper.isPrimitive())
			return wrapper;
		if (wrapper == Boolean.TYPE)
			return Boolean.class;
		if (wrapper == Byte.TYPE)
			return Byte.class;
		if (wrapper == Character.TYPE)
			return Character.class;
		if (wrapper == Short.TYPE)
			return Short.class;
		if (wrapper == Integer.TYPE)
			return Integer.class;
		if (wrapper == Long.TYPE)
			return Long.class;
		if (wrapper == Float.TYPE)
			return Float.class;
		if (wrapper == Double.TYPE)
			return Double.class;
		if (wrapper == Void.TYPE)
			return Void.class;
		throw new ClassCastException("cant make non primitive from :" + wrapper);
	}

}
