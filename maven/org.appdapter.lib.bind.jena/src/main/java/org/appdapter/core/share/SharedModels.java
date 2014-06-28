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
package org.appdapter.core.share;

import java.util.List;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.appdapter.core.share.RemoteDatasetProviderSpec;
// import org.appdapter.core.share.ShareSpec;
import org.appdapter.core.store.DatasetProvider;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface SharedModels extends DatasetProvider {

	/**
	 * @param modelIDs The Dataset's ModelIDs such as: taChan_77, taChan_78 .... null= ALL
	 * @param shareName "robot01" (this makes the remote share Effectively robot01-taChan_77 if there was a global
	 * namespace)
	 * @param clearRemote - upon call this will clear the remote Model
	 * @param clearLocal - upon call this will clear the local Model
	 * @param mergeAfterClear - after remote or local is cleared there may be data on both ends.. this says to add the
	 * theres data to both ends
	 * @param isSharedAfterMerge - set true if the model will now be using what is on the remote end
	 * @param remoteDatasetProviderSpec (new SparqlDatasetProvider("http://localhost:3030/sparql") or .. new
	 * SDBDatasetProvider("foo.ttl"); new RepoDatasetProvider(myOtherRepo); new
	 * RealDatasetProvider(DatsetFactory.createMem());
	 */
	public void setNamedModelShareType(List<Ident> modelIDs, String shareName, boolean clearRemote, boolean clearLocal, boolean mergeAfterClear, boolean isSharedAfterMerge,
		RemoteDatasetProviderSpec remoteDatasetProviderSpec);

	public void setNamedModelShareType(List<ShareSpec> shareSpecs, RemoteDatasetProviderSpec remoteDatasetProviderSpec);

	public Map<Ident, ShareSpec> getSharedModelSpecs();
}
