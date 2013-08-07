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

/*
 * ModelMatrixPanel.java
 *
 * Created on Oct 25, 2010, 8:12:03 PM
 */

package org.appdapter.gui.repo;

import java.util.List;

import org.appdapter.api.trigger.Box;
import org.appdapter.bind.rdf.jena.model.JenaLiteralUtils;
import org.appdapter.gui.browse.ResourceToFromString;
import org.appdapter.gui.browse.ToFromKeyConverter;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.PropertyValueControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelMatrixPanel<BoxType extends Box> extends GenericMatrixPanel {
	static Logger theLogger = LoggerFactory.getLogger(ModelMatrixPanel.class);

	@UISalient static public ModelMatrixPanel showModelMatrixPanel(final Model obj) {
		return new ModelMatrixPanel() {
			{
				setObject(obj);
			}
		};
	}

	public ToFromKeyConverter getCellConverter(Class valueClazz) {
		if (valueClazz.isAssignableFrom(Resource.class))
			return new ResourceToFromString(getJenaModel());
		return super.getCellConverter(valueClazz);
	}

	public Object convertWas(Object was, Object aValue, Class objNeedsToBe) {
		if (objNeedsToBe.isInstance(aValue))
			return aValue;
		Resource wasr = null;
		if (was instanceof Resource) {

		}
		return Utility.recastCC(aValue, objNeedsToBe);
	}

	static String[] columnNames = new String[] { "Subject", "Predicate", "Object" };//, "Model" };

	public ModelMatrixPanel() {
		super(Model.class, Statement.class, null, columnNames);
		cellClass = Resource.class;
	}

	@Override public String getTextName(Object value, int edit_row, int edit_col) {
		if (value == null)
			return "(Resource)null";
		Object val = JenaLiteralUtils.cvtToString(value, getPrefixMapping());
		return "" + val;
	}

	PrefixMapping mapping = null;

	private PrefixMapping getPrefixMapping() {
		if (mapping == null) {
			Model jm = getJenaModel();
			if (jm instanceof PrefixMapping) {
				mapping = (PrefixMapping) jm;
			}
		}
		return mapping;
	}

	private Model getJenaModel() {
		Object o = getValue();
		if (o == null)
			return null;
		return Utility.recastCC(o, Model.class);
	}

	protected List listFromHolder(Object o) {
		if (o == null)
			return null;
		Model model = Utility.recastCC(o, Model.class);
		return model.listStatements().toList();
	}

}
