package org.appdapter.gui.box;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.appdapter.api.trigger.ABoxImpl;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.gui.util.CollectionSetUtils;

public class BoxImpl<TrigType extends Trigger<? extends ABoxImpl<TrigType>>> extends ABoxImpl<TrigType> {

	/** 
	 * This returns the decomposed Mixins
	 * @return
	 */
	public Iterable<Object> getObjects() {
		Object o = getValue();
		if (o != null && o != this) {
			return Arrays.asList(new Object[] { o, this, getIdent(), getShortLabel() });
		}
		return Arrays.asList(new Object[] { this, getIdent(), getShortLabel(), });
	}

	@Override public <T, E extends T> Iterable<E> getObjects(Class<T> type) {
		HashSet<E> objs = new HashSet<E>();
		for (Object o : getObjects()) {
			if (type.isInstance(o)) {
				objs.add((E) o);
			}
		}
		return objs;
	}

	@Override public Object getValue() {
		return this;
	}

}
