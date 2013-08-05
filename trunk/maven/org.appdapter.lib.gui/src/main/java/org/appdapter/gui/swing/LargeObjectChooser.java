package org.appdapter.gui.swing;

import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;

/**
 * A GUI component showing the list of objects in the collection
 */
public class LargeObjectChooser extends CollectionContentsPanel implements POJOCollectionListener {

	public LargeObjectChooser(Class filterc, NamedObjectCollection noc) {
		super(null, noc.getName(), null, filterc, noc, null, true);
		this.valueIsOneSelectedItem = true;
		this.filter = filterc;
		this.localCollection = noc;
		noc.addListener(this, true);
	}

}