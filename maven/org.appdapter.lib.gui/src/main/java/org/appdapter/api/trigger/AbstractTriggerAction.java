package org.appdapter.api.trigger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.appdapter.core.boot.ClassLoaderUtils;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.FileStreamUtils;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.rimpl.TriggerMenuFactory;
import org.appdapter.gui.util.CollectionSetUtils;
import org.appdapter.gui.util.PromiscuousClassUtils;

abstract public class AbstractTriggerAction extends AbstractAction implements MutableTrigger, KnownComponent {

	public BT boxed;
	public Object value;
	protected NamedObjectCollection currentCollection;

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
