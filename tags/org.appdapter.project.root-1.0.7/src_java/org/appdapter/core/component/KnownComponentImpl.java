/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.component;

import org.appdapter.core.item.Ident;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class KnownComponentImpl extends BasicDebugger implements MutableKnownComponent {
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
		myDescription = description;
	}

	@Override public String getShortLabel() {
		return myShortLabel;
	}

	@Override public void setShortLabel(String shortLabel) {
		this.myShortLabel = shortLabel;
	}
	public String getFieldSummary() {
		return "desc=" + myDescription;
	}

	@Override public String toString() {
		return this.getClass().getSimpleName() + "-" + hashCode() + "-" + getShortLabel() + "[" + getFieldSummary() + "]";
	}
}
