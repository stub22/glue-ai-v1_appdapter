/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.gui.box;

import com.appdapter.core.item.Ident;

/**
 * @author Stu B. <www.texpedient.com>
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
