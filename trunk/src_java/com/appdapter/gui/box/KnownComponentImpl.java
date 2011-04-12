/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import com.appdapter.core.item.Ident;

/**
 *
 * @author winston
 */
public abstract class KnownComponentImpl implements MutableKnownComponent {
	private	Ident	myIdent;
	private String	myShortLabel, myDescription;


	@Override public void setIdent(Ident id) {
		myIdent = id;
	}

	@Override public Ident getIdent() {
		return myIdent;
	}

	@Override public String getDescription() {
		return myDescription;
	}

	@Override public void setDescription(String description) {
		this.myDescription = description;
	}

	@Override public String getShortLabel() {
		return myShortLabel;
	}

	@Override public void setShortLabel(String shortLabel) {
		this.myShortLabel = shortLabel;
	}
	protected abstract String getFieldSummary();

	@Override public String toString() {
		return this.getClass().getSimpleName() + "-" + getShortLabel() + "[" + getFieldSummary() + "]";
	}
}
