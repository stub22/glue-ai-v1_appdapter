package org.appdapter.core.loader;

import org.appdapter.core.loader.SpecialRepoLoader;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/*
 *  Copyright 2013 by The Appdapter Project (www.appdapter.org).
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
public interface InstallableRepoLoader {

	boolean isDerivedLoader();

	String getContainerType();

	void loadModelsIntoTargetDataset(SpecialRepoLoader repo, Dataset mainDset,
			Model dirModel, java.util.List<ClassLoader> fileModelCLs);

}
