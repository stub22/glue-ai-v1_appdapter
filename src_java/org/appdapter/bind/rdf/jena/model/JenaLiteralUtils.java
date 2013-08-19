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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.appdapter.api.trigger.AnyOper.HasIdent;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.component.IdentToObjectListener;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.Converter;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.name.ModelIdent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

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

	static public <T> Object convertOrNull(Object obj, Class<T> objNeedsToBe) {
		Object eval = obj;
		boolean findComponent = KnownComponent.class.isAssignableFrom(objNeedsToBe);
		if (obj instanceof ModelIdent) {
			obj = ((ModelIdent) obj).getJenaResource();
		}
		if (obj instanceof RDFNode) {
			eval = JenaLiteralUtils.convertRDFNodeStatic((RDFNode) obj, objNeedsToBe, findComponent);
			if (objNeedsToBe.isInstance(eval))
				return eval;
			if (eval != null) {
				obj = eval;
			}
		}
		if (obj instanceof HasIdent) {
			eval = JenaLiteralUtils.convertIdentNodeStatic(((HasIdent) obj).getIdent(), objNeedsToBe, findComponent);
			if (objNeedsToBe.isInstance(eval))
				return eval;
			if (eval != null) {
				obj = eval;
			}
		}
		return eval;
	}

	public static <T> T convertRDFNodeStatic(RDFNode e, Class<T> type, boolean findComponent) {
		Node node = e.asNode();
		type = ReflectUtils.nonPrimitiveTypeFor(type);
		if (Number.class.isAssignableFrom(type) || java.math.BigInteger.class.isAssignableFrom(type)) {
			return (T) node.getLiteral().getValue();
		}
		if (Boolean.class.isAssignableFrom(type)) {
			return (T) node.getLiteral().getValue();
		}
		if (Character.class.isAssignableFrom(type)) {
			return (T) node.getLiteral().getValue();
		}
		if (false) {
			if (Integer.class.isAssignableFrom(type)) {
				return (T) (Integer) node.getLiteral().getValue();
			}
			if (Double.class.isAssignableFrom(type)) {
				return (T) (Double) e.asLiteral().getDouble();
			}
			if (Byte.class.isAssignableFrom(type)) {
				return (T) (Byte) e.asLiteral().getByte();
			}
			if (Character.class.isAssignableFrom(type)) {
				return (T) (Character) e.asLiteral().getChar();
			}
		}
		if (RDFNode.class.isAssignableFrom(type)) {
			Class<? extends RDFNode> rtype = (Class<? extends RDFNode>) type;

			if (e.canAs(rtype))
				return (T) e.as(rtype);
		}
		if (Ident.class.isAssignableFrom(type)) {
			return (T) new FreeIdent(e.asNode().getURI());
		}
		if (String.class.isAssignableFrom(type)) {
			try {
				String lv = node.toString(false);
				lv = unquote(lv);
				return (T) lv;
			} catch (Exception ex) {
				Debuggable.printStackTrace(ex);
			}
		}
		Object eval = node;
		if (e.isLiteral()) {
			LiteralLabel lit = node.getLiteral();
			RDFDatatype dt = lit.getDatatype();
			if (dt != null) {
				Class clz = dt.getJavaClass();
				if (clz != null && clz != type) {
					return (T) convertRDFNodeStatic(e, clz, findComponent);
				}
			}
			eval = lit.getValue();
			if (!(eval instanceof String)) {
				return (T) eval;
			}
			return (T) lit;
		} else if (e.isURIResource()) {
			if (findComponent) {
				String uri = node.getURI();
				Ident id = new FreeIdent(uri);
				eval = findComponent(id, type);
				if (eval == null) {
					eval = node;
				}
			}
		}
		if (e == eval) {
			return (T) e;
		}
		return (T) eval;
	}

	public static <T> T convertIdentNodeStatic(Ident id, Class<T> type, boolean findComponent) {
		Object eval = id;
		if (findComponent) {
			eval = findComponent(id, type);
			if (eval == null) {
				eval = id;
			}
		}
		return (T) eval;
	}

	private static String unquote(String lv) {
		int len = lv.length();
		if (!lv.startsWith("\"") || !lv.endsWith("\""))
			return lv;
		String clv = lv.substring(1, len - 2);
		if (!clv.endsWith("\\") && !clv.contains("\"")) {
			return clv;
		}
		return lv;
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

	public static Object cvtToString(Object value, PrefixMapping mapping) {
		if (value == null)
			return null;
		Object val = JenaLiteralUtils.convertOrNull(value, Object.class);
		if (val instanceof Node_URI) {
			if (mapping == null) {
				if (value instanceof PrefixMapping) {
					mapping = (PrefixMapping) value;
				} else {
					if (value instanceof ModelIdent) {
						value = ((ModelIdent) value).getJenaResource();
					}
					if (value instanceof Resource) {
						mapping = ((Resource) value).getModel();
					}
				}
			}
			if (mapping != null) {
				return ((Node_URI) val).toString(mapping, true);
			} else {
				return ((Node_URI) val).toString(true);
			}
		}
		if (val instanceof Literal) {
			return val;
		}
		return val;
	}

}
