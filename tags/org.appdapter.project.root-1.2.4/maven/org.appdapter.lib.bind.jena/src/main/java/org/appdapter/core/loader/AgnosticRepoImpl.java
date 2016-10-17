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

package org.appdapter.core.loader;

import org.appdapter.core.name.Ident;
import org.appdapter.core.store.RepoOper;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 */

public abstract class AgnosticRepoImpl extends LoadingRepoImpl {
	private UserDatasetFactory myDatasetProvider = RepoDatasetFactory.getDefaultUserDF();
	private String datasetType;
	
	protected AgnosticRepoImpl(SpecialRepoLoader srepoLoader) {
		super(srepoLoader);
	}
	protected AgnosticRepoImpl() { 
		super();
	}
	
	protected String getDatasetType() {
		if (myDatasetProvider != null)
			return myDatasetProvider.getDatasetType();
		return datasetType;
	}
	
	@Override protected Dataset makeMainQueryDataset() {
		if (myDatasetProvider != null) {
			getLogger().info("Using datasetProvider {}", myDatasetProvider);
			return myDatasetProvider.createDefault();
		}
		if (datasetType != null) {
			getLogger().warn("Would use datasetType ?{} if we knew how, but we don't, and the datasetProvider is null!", datasetType);
			// Stu found this dead code layin here, would produce NPE every time, right?
			// return datasetProvider.createDefault();
		}
		getLogger().info("Using RepoDatasetFactory.createDefault()");
		Dataset ds = RepoDatasetFactory.createDefault(); // becomes   createMem() in later Jena versions.
		return ds;
	}	

}
