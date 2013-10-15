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
package org.appdapter.core.store;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.bind.rdf.jena.sdb.SdbStoreFactory;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.store.RepoOper.ReloadableDataset;
import org.appdapter.demo.DemoDatabase;
import org.appdapter.demo.DemoResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.StoreFormatter;

// import com.hp.hpl.jena.query.DataSource;

/**
 * @author Dmiles
 */

@UIHidden
public class RepoDatasetFactory implements AnyOper, UtilClass {

	static Logger theLogger = LoggerFactory.getLogger(RepoDatasetFactory.class);

	/**
	 *  To share Repos between JVM instances
	 *   alwaysShareDataset = true;
	 */
	final public static String DATASET_TYPE_DEFAULT_MEMFILE_TYPE = "memory";
	final public static String DATASET_TYPE_DEFAULT_SHARE_TYPE = "shared";
	public static boolean alwaysShareDataset = false;
	public static boolean alwaysShareDatasetHack = false;
	public static String DATASET_TYPE_SHARED = DATASET_TYPE_DEFAULT_SHARE_TYPE;
	public static String DATASET_TYPE_DEFAULT = DATASET_TYPE_DEFAULT_MEMFILE_TYPE;
	public static String DATASET_SHARE_NAME = "robot01";

	@UISalient public static Dataset createMem() {
		if (alwaysShareDatasetHack)
			return createShared();
		return createDataset(DATASET_TYPE_DEFAULT_MEMFILE_TYPE);
	}

	@UISalient public static Dataset createDefault() {
		if (alwaysShareDatasetHack)
			return createShared();
		return createDataset(DATASET_TYPE_DEFAULT);
	}

	@UISalient public static Dataset createShared() {
		Dataset memDataset = DatasetFactory.createMem();
		return linkWithShared(memDataset);
	}

	public static Dataset linkWithShared(Dataset memDataset) {
		addDatasetSync(memDataset, getGlobalDShared());
		return memDataset;
	}

	@UISalient public static Dataset createDataset(String typeOf) {
		return createDataset(typeOf, DATASET_SHARE_NAME);
	}

	public static void registerDatasetFactory(String datasetTypeName, UserDatasetFactory udf) {
		Map<String, UserDatasetFactory> dsfMap = UserDatasetFactory.registeredUserDatasetFactoryByName;
		synchronized (dsfMap) {
			dsfMap.put(datasetTypeName, udf);
		}
		List<UserDatasetFactory> lst = UserDatasetFactory.registeredUserDatasetFactorys;
		synchronized (lst) {
			lst.remove(udf);
			lst.add(0, udf);
		}
	}

	@UISalient public static Dataset createDataset(String typeOf, String sharedNameIgnoredPresently) {
		if (typeOf == null) {
			typeOf = DATASET_TYPE_DEFAULT;
		} else {
			typeOf = typeOf.toLowerCase();
		}
		Map<String, UserDatasetFactory> dsfMap = UserDatasetFactory.registeredUserDatasetFactoryByName;
		UserDatasetFactory udsf0 = null;
		synchronized (dsfMap) {
			udsf0 = dsfMap.get(typeOf);
		}
		if (udsf0 != null) {
			return udsf0.createType(typeOf, sharedNameIgnoredPresently);
		}
		for (UserDatasetFactory udsf : getRegisteredUserDatasetFactories()) {
			if (udsf.canCreateType(typeOf, sharedNameIgnoredPresently)) {
				return udsf.createType(typeOf, sharedNameIgnoredPresently);
			}
		}
		return UserDatasetFactory.jenaDatasetFactory.createType(typeOf, sharedNameIgnoredPresently);
	}

	private static List<UserDatasetFactory> getRegisteredUserDatasetFactories() {
		return ReflectUtils.copyOf(UserDatasetFactory.registeredUserDatasetFactorys);
	}

	@UISalient public static void replaceWithDB(ReloadableDataset myRepo, Resource unionOrReplace) {
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = getGlobalDShared();
		myRepo.setMyMainQueryDataset(newDs);
		RepoOper.replaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	@UISalient public static void replaceViaFactory(ReloadableDataset myRepo, UserDatasetFactory factory, Resource unionOrReplace) {
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = factory.create(oldDs);
		myRepo.setMyMainQueryDataset(newDs);
		RepoOper.replaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	@UISalient public static void replaceWitMemory(ReloadableDataset myRepo, Resource unionOrReplace) {
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = DatasetFactory.createMem();
		myRepo.setMyMainQueryDataset(newDs);
		RepoOper.replaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	@UISalient public static void addModelSync(Model m1, Model m2) {
		StatementSync ss = StatementSync.getStatementSyncerOfModels(m1, m2);
		ss.enableSync();
		ss.completeSync();
	}

	@UISalient public static void addDatasetSync(Dataset d1, Dataset d2) {
		HashSet<String> nameSet = new HashSet<String>();
		ReflectUtils.addAllNew(nameSet, d1.listNames());
		ReflectUtils.addAllNew(nameSet, d2.listNames());
		for (String uri : nameSet) {
			addModelSync(uri, d1, d2);
		}
	}

	private static void addModelSync(String uri, Dataset... dsDatasets) {
		Model model = findOrCreateGlobalModel(uri);
		for (Dataset newDs : dsDatasets) {
			addModelSync(model, findOrCreateModel(newDs, uri));
		}
	}

	private static Model findOrCreateModel(Dataset newDs, String uri) {
		if (!newDs.containsNamedModel(uri)) {
			newDs.addNamedModel(uri, createModelForDataset(uri, newDs));
		}
		return newDs.getNamedModel(uri);
	}

	private static Model createModelForDataset(String uri, Dataset newDs) {
		return ModelFactory.createDefaultModel();
	}

	public static Dataset globalDS = null;

	public static Model findOrCreateGlobalModel(String uri) {
		return findOrCreateModel(getGlobalDShared(), uri);
	}

	public synchronized static Dataset getGlobalDShared() {
		if (globalDS == null) {
			SDB.getContext();
			globalDS = connectDataset(DemoResources.STORE_CONFIG_PATH);
		}
		return DatasetFactory.create(globalDS);
	}

	private static Dataset connectDataset(String storeConfigPath) {
		SDB.init();
		ClassLoader classLoader = RepoOper.class.getClassLoader();
		JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(classLoader);
		Store store = SdbStoreFactory.connectSdbStoreFromResPath(storeConfigPath, classLoader);
		try {
			SDBFactory.connectDataset(store).listNames();
		} catch (Exception e) {
			ensureQuadStore(store);
		}
		//ensureQuadStore(store);
		Dataset dataset = SDBFactory.connectDataset(store);
		return dataset;
	}

	private static void ensureQuadStore(Store store) {
		DemoDatabase.initConnector();
		StoreFormatter formaterObject = store.getTableFormatter();
		formaterObject.create();
		formaterObject.format();
		formaterObject.dropIndexes();
		formaterObject.addIndexes();
	}

}
