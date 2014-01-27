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
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.bind.rdf.jena.sdb.SdbStoreFactory;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.debug.UIAnnotations.UIHidden;
import org.appdapter.core.debug.UIAnnotations.UtilClass;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.store.RepoOper;
import org.appdapter.core.store.RepoOper.ReloadableDataset;
import org.appdapter.core.store.StatementSync;
import org.appdapter.demo.DemoDatabase;
import org.appdapter.demo.DemoResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Factory;
//import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.StoreFormatter;

/**
 * @author Logicmoo. <www.logicmoo.org>
 * 
 *         Handling for a local *or* some 'remote'/'shared' model/dataset impl.
 * 
 */
@UIHidden
public class RepoDatasetFactory implements AnyOper, UtilClass {
	static Logger theLogger = LoggerFactory.getLogger(RepoDatasetFactory.class);

	public static boolean verifyURI(String uri) {
		return FreeIdent.verifyURI(uri);
	}

	public static String fixURI(String uri) {
		return FreeIdent.previousURI(uri);
	}

	@UISalient
	public static boolean allModelNoDelete = true;
	@UISalient
	public static boolean datasetNoDeleteModels = true;
	@UISalient
	public static boolean allModelsTheSame = false;

	public static boolean alwaysShareDataset = false;

	public static boolean allwaysRenameModels = false;

	final public static String DATASET_TYPE_DEFAULT_MEMFILE_TYPE = "memory";
	final public static String DATASET_TYPE_DEFAULT_SHARE_TYPE = "shared";
	public static String DATASET_SHARE_NAME = "robot01";
	public static String DATASET_TYPE_UNSHARED = DATASET_TYPE_DEFAULT_MEMFILE_TYPE;
	public static String DATASET_TYPE_DEFAULT = DATASET_TYPE_UNSHARED;
	public static String DATASET_TYPE_SHARED = DATASET_TYPE_DEFAULT_SHARE_TYPE;
	static final Map<String, String> datasetClassTypesMap = new HashMap();
	public static UserDatasetFactory DEFAULT;
	public static Dataset globalDS = null;
	static long serialNumber = 111666;
	static final UserDatasetFactory jenaSDBDatasetFactory = new JenaSDBWrappedDatasetFactory();
	static final UserDatasetFactory jenaUnsharedMemoryDatasetFactory = new JenaDatasetFactory();
	static final Map<String, UserDatasetFactory> registeredUserDatasetFactoryByName = new HashMap<String, UserDatasetFactory>();
	static final List<UserDatasetFactory> registeredUserDatasetFactorys = new ArrayList<UserDatasetFactory>();
	private static final boolean VANILLA = false;

	/**
	 * To share Repos between JVM instances alwaysShareDataset = true;
	 */
	public static String STORE_CONFIG_PATH = DemoResources.STORE_CONFIG_PATH;

	static Model universalModel = null;

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

	public static Model createModelForGraph(Graph graph) {
		if (!(graph instanceof CheckedGraph)) {
			graph = new CheckedGraph(graph, false, false, true);
		}
		//if (false)	return ModelFactory.createModelForGraph(graph);
		return new CheckedModel((CheckedGraph) graph);

	}

	static public void invalidateModel(final Model m) {
		if (m == universalModel) {
			return;
		}
		m.register(new StatementListener() {
			@Override public void addedStatement(Statement s) {
				super.addedStatement(s);
				throw new RuntimeException("addNoMods: Dead Model " + m);
			}

			@Override public void removedStatement(Statement s) {
				super.removedStatement(s);
				throw new RuntimeException("addNoMods: Dead Model " + m);
			}
		});

	}

	@UISalient public static void addModelSync(Model m1, Model m2) {
		if (true)
			Debuggable.notImplemented("addModelSync", m1, m2);
		StatementSync ss = StatementSync.getStatementSyncerOfModels(m1, m2);
		ss.enableSync();
		ss.completeSync();
	}

	private static void addModelSync(String uri, Dataset... dsDatasets) {
		untested();
		Model model = findOrCreateGlobalModel(uri);
		for (Dataset newDs : dsDatasets) {
			addModelSync(model, findOrCreateModel(newDs, uri));
		}
	}

	private static Dataset connectDataset(String storeConfigPath) {
		untested();
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

	@UISalient public static Dataset createDataset(String typeOf) {
		return createDataset(typeOf, DATASET_SHARE_NAME);
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

	@UISalient public static Dataset createMem() {
		if (alwaysShareDataset)
			return createShared();
		return createDataset(DATASET_TYPE_DEFAULT_MEMFILE_TYPE);
	}

	@UISalient public static Dataset createDefault() {
		if (alwaysShareDataset)
			return createShared();
		return createDataset(DATASET_TYPE_DEFAULT);
	}

	public static Model createUnion(Model m1, Model m2) {
		Model shared = createUnionNoPrefixShare(m1, m2);
		shared.withDefaultMappings(m1);
		shared.withDefaultMappings(m2);
		//ModelCom.class.getName();
		return shared;
	}

	public static Model createUnionNoPrefixShare(Model m1, Model m2) {
		if (allModelNoDelete)
			return wrapNoDelete(createUnionImpl(m1, m2));
		return wrapPrefixCheck(createUnionImpl(m1, m2));
	}

	private static Model createUnionImpl(Model m1, Model m2) {
		if (VANILLA) {
			ModelFactory.createUnion(m1, m2);
		}
		return createModelForGraph(new Union(m1.getGraph(), m2.getGraph()));
	}

	public static Model createDefaultModelNoDelete() {
		final Model nonuniversalModel = createDefaultModelImpl();
		return wrapPrefixCheck(wrapNoDelete(nonuniversalModel));
	}

	private static Model createDefaultModelImpl() {
		if (false) {
			return createModel(DATASET_TYPE_UNSHARED);
		}
		return new CheckedModel(Factory.createGraphMem(), false, false, false);
	}

	public static Model createPrivateMemModel() {
		if (allModelNoDelete)
			return createDefaultModelNoDelete();
		return wrapPrefixCheck(createDefaultModelImpl());
	}

	public static Model createDefaultModel() {
		if (allModelsTheSame) {
			return getSharedModel();
		}
		if (allModelNoDelete)
			return createDefaultModelNoDelete();
		return RepoDatasetFactory.createPrivateMemModel();
	}

	public static Model wrapNoDelete(final Model nonuniversalModel) {
		final Graph modelGraph = nonuniversalModel.getGraph();
		if (modelGraph instanceof CheckedGraph) {
			((CheckedGraph) modelGraph).setNoDelete(true);
			return nonuniversalModel;
		}
		return createModelForGraph(new CheckedGraph(modelGraph, false, true, true));
	}

	public static Model wrapReadOnly(final Model nonuniversalModel) {
		final Graph modelGraph = nonuniversalModel.getGraph();
		if (modelGraph instanceof CheckedGraph) {
			((CheckedGraph) modelGraph).setNoAdd(true);
			((CheckedGraph) modelGraph).setNoDelete(true);
			return nonuniversalModel;
		}
		return createModelForGraph(new CheckedGraph(modelGraph, true, true, true));
	}

	public static Model wrapPrefixCheck(Model nonuniversalModel) {
		final Graph modelGraph = nonuniversalModel.getGraph();
		if (modelGraph instanceof CheckedGraph) {
			((CheckedGraph) modelGraph).setPrefixCheck(true);
			return nonuniversalModel;
		}
		return new CheckedModel(new CheckedGraph(modelGraph, false, false, true));
	}

	public static Model createModel(String typeOf) {
		return createModel(typeOf, null, DATASET_SHARE_NAME);
	}

	private static Model createModelForDataset(String uri, Dataset newDs) {
		return createDefaultModel();
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

	static public String createNewName() {
		serialNumber++;
		if (true)
			return "S" + serialNumber;
		String newID = UUID.randomUUID().toString();
		String newName = (newID).replace('-', '_');
		return newName;
	}

	public static Dataset createPrivateMem() {
		if (alwaysShareDataset)
			return getGlobalDShared();
		return new CheckedDataset(DatasetFactory.createMem());
	}

	@UISalient public static Dataset createShared() {
		if (alwaysShareDataset)
			return getGlobalDShared();
		Dataset memDataset = createMem();
		return linkWithShared(memDataset);
	}

	private static void ensureQuadStore(Store store) {
		DemoDatabase.initConnector();
		StoreFormatter formaterObject = store.getTableFormatter();
		formaterObject.create();
		formaterObject.format();
		formaterObject.dropIndexes();
		formaterObject.addIndexes();
	}

	public static Model findOrCreateGlobalModel(String uri) {
		return findOrCreateModel(getGlobalDShared(), uri);
	}

	public static Model findOrCreateModel(Dataset newDs, String uri) {
		uri = RepoOper.correctModelName(uri);
		if (!newDs.containsNamedModel(uri)) {
			newDs.addNamedModel(uri, createModelForDataset(uri, newDs));
		}
		return newDs.getNamedModel(uri);
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

	public synchronized static Dataset getGlobalDShared() {

		if (globalDS == null) {
			if (allModelNoDelete) {
				globalDS = new CheckedDataset();
			}
		}
		if (globalDS == null) {
			SDB.getContext();
			globalDS = connectDataset(STORE_CONFIG_PATH);
		}
		if (alwaysShareDataset) {
			return globalDS;
		}
		return DatasetFactory.create(globalDS);
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

	private static List<UserDatasetFactory> getRegisteredUserDatasetFactories() {
		return ReflectUtils.copyOf(registeredUserDatasetFactorys);
	}

	public static Model getSharedModel() {
		if (universalModel == null) {
			universalModel = RepoDatasetFactory.createDefaultModelNoDelete();
		}
		return universalModel;
	}

	public static Dataset linkWithShared(Dataset memDataset) {
		untested();
		addDatasetSync(memDataset, getGlobalDShared());
		return memDataset;
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

	@UISalient public static void addOrReplaceViaFactory(ReloadableDataset myRepo, UserDatasetFactory factory, Resource unionOrReplace) {
		untested();
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = factory.create(oldDs);
		myRepo.setMyMainQueryDataset(newDs);
		RepoOper.addOrReplaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	@UISalient public static void addOrReplaceWithDB(ReloadableDataset myRepo, Resource unionOrReplace) {
		untested();
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = getGlobalDShared();
		myRepo.setMyMainQueryDataset(newDs);
		RepoOper.addOrReplaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	@UISalient public static void addOrReplaceWitMemory(ReloadableDataset myRepo, Resource unionOrReplace) {
		untested();
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = DatasetFactory.createMem();
		myRepo.setMyMainQueryDataset(newDs);
		RepoOper.addOrReplaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	static void untested(Object... args) {
		if (true)
			Debuggable.notImplemented(args);
	}

	public static Node correctModelName(String onlyModel) {
		if (allwaysRenameModels) {
			if (onlyModel.endsWith("_22")) {
				//onlyModel = onlyModel.substring(0, onlyModel.length() - 2) + "_77";
			}
			if (onlyModel.endsWith("#")) {
				onlyModel = onlyModel.substring(0, onlyModel.length() - 1);
				return Node.createURI(correctModelName(onlyModel) + "#");
			}
			boolean remove = true;
			while (remove) {
				remove = false;
				char[] array = "1234567890_".toCharArray();
				for (int i = 0; i < array.length; i++) {
					char c = array[i];
					if (onlyModel.endsWith("" + c)) {
						onlyModel = onlyModel.substring(0, onlyModel.length() - 1);
						remove = true;
						break;
					}
				}
			}
		}
		return Node.createURI(onlyModel);
	}
}
