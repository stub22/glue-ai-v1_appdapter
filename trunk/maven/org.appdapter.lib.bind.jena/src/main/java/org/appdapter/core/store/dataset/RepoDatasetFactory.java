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
package org.appdapter.core.store.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.core.debug.UIAnnotations.UIHidden;
import org.appdapter.core.debug.UIAnnotations.UtilClass;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.bind.rdf.jena.sdb.SdbStoreFactory;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.store.RepoOper;
import org.appdapter.core.store.RepoOper.ReloadableDataset;
import org.appdapter.core.store.StatementSync;
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

/**
 * @author Logicmoo. <www.logicmoo.org>
 *
 * Handling for a local *or* some 'remote'/'shared' model/dataset impl.
 *
 */
@UIHidden
public class RepoDatasetFactory implements AnyOper, UtilClass {

	static Logger theLogger = LoggerFactory.getLogger(RepoDatasetFactory.class);
	static final Map<String, String> datasetClassTypesMap = new HashMap();
	static final Map<String, UserDatasetFactory> registeredUserDatasetFactoryByName = new HashMap<String, UserDatasetFactory>();
	static final List<UserDatasetFactory> registeredUserDatasetFactorys = new ArrayList<UserDatasetFactory>();
	static final UserDatasetFactory jenaUnsharedMemoryDatasetFactory = new JenaDatasetFactory();
	static final UserDatasetFactory jenaSDBDatasetFactory = new JenaSDBWrappedDatasetFactory();
	/**
	 *  To share Repos between JVM instances
	 *   alwaysShareDataset = true;
	 */
	public static String STORE_CONFIG_PATH = DemoResources.STORE_CONFIG_PATH;
	final public static String DATASET_TYPE_DEFAULT_MEMFILE_TYPE = "memory";
	final public static String DATASET_TYPE_DEFAULT_SHARE_TYPE = "shared";
	public static boolean alwaysShareDataset = false;
	final public static boolean alwaysShareDatasetHack = false;
	public static String DATASET_TYPE_SHARED = DATASET_TYPE_DEFAULT_SHARE_TYPE;
	public static String DATASET_TYPE_UNSHARED = DATASET_TYPE_DEFAULT_MEMFILE_TYPE;
	public static String DATASET_TYPE_DEFAULT = DATASET_TYPE_UNSHARED;
	public static String DATASET_SHARE_NAME = "robot01";

	static {
		registerDatasetFactory("default", jenaUnsharedMemoryDatasetFactory);
		registerDatasetFactory("jena", jenaUnsharedMemoryDatasetFactory);
		registerDatasetFactory("memory", jenaUnsharedMemoryDatasetFactory);
		registerDatasetFactory("instance", jenaSDBDatasetFactory);
		registerDatasetFactory("database", jenaSDBDatasetFactory);
		registerDatasetFactory("shared", jenaSDBDatasetFactory);
		registerDatasetFactory(DATASET_TYPE_DEFAULT_MEMFILE_TYPE, jenaUnsharedMemoryDatasetFactory);
		registerDatasetFactory(DATASET_TYPE_DEFAULT_SHARE_TYPE, jenaSDBDatasetFactory);
	}

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
		Map<String, UserDatasetFactory> dsfMap = registeredUserDatasetFactoryByName;
		synchronized (dsfMap) {
			dsfMap.put(datasetTypeName, udf);
		}
		List<UserDatasetFactory> lst = registeredUserDatasetFactorys;
		synchronized (lst) {
			lst.remove(udf);
			lst.add(0, udf);
		}
	}

	public static Dataset createPrivateMem() {
		return DatasetFactory.createMem();
	}

	public static Model createPrivateMemModel() {
		return createModel(DATASET_TYPE_UNSHARED);
	}

	@UISalient public static Dataset createDataset(String typeOf, String shareName) {
		Dataset ds = createDataset0(typeOf, shareName);
		if (typeOf != null) {
			synchronized (datasetClassTypesMap) {
				datasetClassTypesMap.put(ds.getClass().getName(), typeOf);
			}
		}
		return ds;
	}

	static Dataset createDataset0(String typeOf, String shareName) {
		if (typeOf == null) {
			typeOf = DATASET_TYPE_DEFAULT;
		} else {
			typeOf = typeOf.toLowerCase();
		}
		Map<String, UserDatasetFactory> dsfMap = registeredUserDatasetFactoryByName;
		UserDatasetFactory udsf0 = null;
		synchronized (dsfMap) {
			udsf0 = dsfMap.get(typeOf);
		}
		if (udsf0 != null) {
			return udsf0.createType(typeOf, shareName);
		}
		for (UserDatasetFactory udsf : getRegisteredUserDatasetFactories()) {
			if (udsf.canCreateType(typeOf, shareName)) {
				return udsf.createType(typeOf, shareName);
			}
		}
		return jenaUnsharedMemoryDatasetFactory.createType(typeOf, shareName);
	}

	@UISalient public static Model createModel(String typeOf, String modelName, String shareName) {
		if (typeOf == null) {
			typeOf = DATASET_TYPE_DEFAULT;
		} else {
			typeOf = typeOf.toLowerCase();
		}
		Map<String, UserDatasetFactory> dsfMap = registeredUserDatasetFactoryByName;
		UserDatasetFactory udsf0 = null;
		synchronized (dsfMap) {
			udsf0 = dsfMap.get(typeOf);
		}
		if (udsf0 != null) {
			return udsf0.createModelOfType(typeOf, modelName, shareName);
		}
		for (UserDatasetFactory udsf : getRegisteredUserDatasetFactories()) {
			if (udsf.canCreateModelOfType(typeOf, shareName)) {
				return udsf.createModelOfType(typeOf, modelName, shareName);
			}
		}
		return jenaUnsharedMemoryDatasetFactory.createModelOfType(typeOf, modelName, shareName);
	}

	private static List<UserDatasetFactory> getRegisteredUserDatasetFactories() {
		return ReflectUtils.copyOf(registeredUserDatasetFactorys);
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
		if (true)
			Debuggable.notImplemented("addModelSync", m1, m2);
		StatementSync ss = StatementSync.getStatementSyncerOfModels(m1, m2);
		ss.enableSync();
		ss.completeSync();
	}

	@UISalient public static void addDatasetSync(Dataset d1, Dataset d2) {
		if (true)
			Debuggable.notImplemented("AddDatasetSync", d1, d2);
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

	public static Model findOrCreateModel(Dataset newDs, String uri) {
		if (!newDs.containsNamedModel(uri)) {
			newDs.addNamedModel(uri, createModelForDataset(uri, newDs));
		}
		return newDs.getNamedModel(uri);
	}

	private static Model createModelForDataset(String uri, Dataset newDs) {
		return ModelFactory.createDefaultModel();
	}

	public static Dataset globalDS = null;
	public static UserDatasetFactory DEFAULT;

	public static Model findOrCreateGlobalModel(String uri) {
		return findOrCreateModel(getGlobalDShared(), uri);
	}

	public synchronized static Dataset getGlobalDShared() {
		if (globalDS == null) {
			SDB.getContext();
			globalDS = connectDataset(STORE_CONFIG_PATH);
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

	public static Model createModel(String typeOf) {
		return createModel(typeOf, null, DATASET_SHARE_NAME);
	}

	public static String getDatasetType(Dataset localDataset) {
		String typeOfString = null;
		synchronized (datasetClassTypesMap) {
			typeOfString = datasetClassTypesMap.get(localDataset.getClass().getName());
			if (typeOfString != null)
				return typeOfString;
		}
		return null;

	}

	static long serialNumber = 111666;

	static public String createNewName() {
		serialNumber++;
		if (true)
			return "S" + serialNumber;
		String newID = UUID.randomUUID().toString();
		String newName = (newID).replace('-', '_');
		return newName;
	}

	public static String getGlobalName(String modelName, String shareName) {
		if (modelName == null) {
			modelName = "Model_" + createNewName();
		}
		if (shareName == null) {
			shareName = DATASET_SHARE_NAME;
		}
		return modelName + "_V_" + shareName;
	}

}
