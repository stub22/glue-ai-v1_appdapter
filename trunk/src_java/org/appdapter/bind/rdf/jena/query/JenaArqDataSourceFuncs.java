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

package org.appdapter.bind.rdf.jena.query;

import org.appdapter.bind.rdf.jena.model.JenaModelUtils;
import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class JenaArqDataSourceFuncs {
	public static BasicDebugger	theDbg = new BasicDebugger();	
/** 	
	  * The exact semantics here are still vague.   Roughly speaking, the new datasource
	  * is one that we can modify "without affecting" the original dataset, but that
	  * meaning is unclear when the original dataset contains database models.
	  * And even in the case of memory models, is Jena making a copy for us?
	  * Need to do some experimenting here.
	**/
	public static DataSource makeIndependentDataSourceFromDataset(Dataset dset) {
		Model newDefaultModel = null;
		Model oldDefaultModel = dset.getDefaultModel();
		if (oldDefaultModel != null) {
			newDefaultModel = JenaModelUtils.makeNaiveCopy(oldDefaultModel);
		}
		Dataset copy = DatasetFactory.make(dset, newDefaultModel);
		// We KNOW this is a DataSource from reading the ARQ 2.1 impl!!!  Ahem.
 		return (DataSource)  copy;
	}	
	
	/** 	
	**/
	public static void ensureDefaultModelNotNull(DataSource ds) {
		if (ds.getDefaultModel() == null) {
			Model emptyModel = ModelFactory.createDefaultModel();
			ds.setDefaultModel(emptyModel);
		}
	}
	
	/** 	If no model yet exists within dataset @ nameURI, then we create it.
			If nameURI is null, then we merge into (or create) the "default model"
	**/
	public static void mergeModelIntoDataSource(DataSource ds, String nameURI, Model m) {
		// Serializing model contents is expensive when the models aren't tiny.
		theDbg.logDebug("Merging in model with name " + nameURI); // + " and contents " + m);

		Model	previousModel = null;
		if (nameURI != null) {
			if (ds.containsNamedModel(nameURI)) {
				previousModel = ds.getNamedModel(nameURI);
			}
		} else {
			previousModel = ds.getDefaultModel();
		}
		Model augmentedModel = null;
		if (previousModel != null) {
			augmentedModel = previousModel;
			previousModel.add(m);
		} else {
			augmentedModel = m;
		}
		if (nameURI != null) {
			ds.replaceNamedModel(nameURI, augmentedModel);
		} else {
			ds.setDefaultModel(augmentedModel);
		}
	}
}
