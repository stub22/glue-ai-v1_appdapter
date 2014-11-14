/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.appdapter.core.jvm.GetObject;
import org.appdapter.core.jvm.SetObject;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;


/**
 * GraphStat was temporarily replaced by this shape.
 * Who uses this?
 */

public class WackyModelPointer extends StatementListener implements Map.Entry<Ident, Model> {

	@Override
	public Ident getKey() {
		return new FreeIdent(graphURI);
	}

	public WackyModelPointer(String uri, GetObject<Model> mdl) {
		this.graphURI = uri;
		model = mdl;
	}
	public String graphURI;
	private GetObject<Model> model;

	/**
	 * Every time this is accessed it reads from the model!
	 */
	public String toString() {
		return "[WackyModelPointer uri=" + graphURI + ", stmtCnt=" + getStatementCount() + "]";
	}

	@Override
	public Model getValue() {
		return model.getValue();
	}

	@Override
	public Model setValue(Model value) {
		Model mdl = model.getValue();
		if (mdl == value) {
			return mdl;
		}
		if (model instanceof SetObject) {
			try {
				((SetObject) model).setObject(value);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		} else {
			throw new UnsupportedOperationException("Cannot change the model in this graphStat to " + value);
		}
		return mdl;
	}

	public long getStatementCount() {
		return model.getValue().size();
	}

}
