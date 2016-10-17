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

package org.appdapter.core.store.dataset;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class JenaTDB_MemoryDatasetFactory   extends JenaDatasetFactory {
	private static Logger theLogger = LoggerFactory.getLogger(JenaTDB_MemoryDatasetFactory.class);
	@Override
	public Dataset createDefault() {
		Dataset dset = TDBFactory.createDataset();
		theLogger.warn("Created TDB-inMemory dataset, supportsTransactions={}", dset.supportsTransactions());
		return dset;
	}

	@Override public String getDatasetType() {
		return RepoDatasetFactory.DFF_TDB_Mem;
	}

}
