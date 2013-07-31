package org.appdapter.gui.trigger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableTrigger;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.browse.KMCTrigger;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.util.CollectionSetUtils;

abstract public class AbstractTriggerAction extends AbstractAction implements KMCTrigger, MutableTrigger, KnownComponent {

	public BT boxed;
	private Object value;
	protected NamedObjectCollection currentCollection;

	/**
	 * @return the value
	 */
	public Object getValue() {
		Object value = this.value;
		if (value == null)
			value = Utility.dref(boxed);
		else {
			value = Utility.dref(value);
		}
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	public AbstractTriggerAction(String name, Icon icon) {
		super(name, icon);
	}

	public AbstractTriggerAction(String name) {
		super(name);
	}

	public AbstractTriggerAction(String... str) {
		super(CollectionSetUtils.join("|", str));
	}

	@Override public void fire(Box targetBox) {
		actionPerformed(null);
	}

	@Override public Ident getIdent() {
		return null;
	}

	@Override public String getDescription() {
		return Debuggable.toInfoStringF(this);
	}

	@Override public String getShortLabel() {
		return "" + getValue(Action.NAME);
	}
}
