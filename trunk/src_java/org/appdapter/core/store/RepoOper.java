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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.demo.DemoResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.shared.Lock;

// import com.hp.hpl.jena.query.DataSource;

/**
 * @author Dmiles
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
@UIHidden
public class RepoOper implements AnyOper, UtilClass {

	@UISalient
	static public interface ISeeToString {
		@Override @UISalient(MenuName = "Call ToString") public String toString();
	}

	@UISalient
	static public interface Reloadable {

		@UISalient(MenuName = "Reload Repo") void reloadAllModels();

		@UISalient() void reloadSingleModel(String modelName);

		@UISalient(ToValueMethod = "toString") Dataset getMainQueryDataset();

		/**
		 * Causes a repo to replace its mainQueryDataset with the 'ds' param
		 *
		 *  To switch from a file repo to a database repo
		 *
		 *     Reloadable myRepo = new URLRepoSpec("myturtle.ttl").makeRepo();
		 *
		 *     Dataset old = myRepo.mainQueryDataset();
		 *
		 *     Dataset newDs = SDB.store();
		 *
		 *
		 *
		 *
		 * @param ds
		 * @return
		 */
		@UISalient(ToValueMethod = "toString") void setMyMainQueryDataset(Dataset ds);
	}

	// static class ConcBootstrapTF extends
	// BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
	// } // TT extends TriggerImpl<BT>
	public static class ReloadAllModelsTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends TriggerImpl<RB> {

		Repo.WithDirectory m_repo;

		// @TODO obviouly we should be using specs and not repos! but
		// With.Directory may as well be the spec for now.
		// Also consider we are using the actual Repo (not the Spec) due to the
		// fact we must have something to clear and update right?
		public ReloadAllModelsTrigger(Repo.WithDirectory repo) {
			m_repo = repo;
		}

		@Override public void fire(RB targetBox) {
			String resolvedQueryURL = DemoResources.QUERY_PATH;
			ClassLoader optCL = getClass().getClassLoader();
			if (targetBox != null) {
				optCL = targetBox.getClass().getClassLoader();
			}
			if (!(m_repo instanceof RepoOper.Reloadable)) {
				theLogger.error("Repo not reloadable! " + targetBox);
			} else {
				RepoOper.Reloadable reloadme = (RepoOper.Reloadable) targetBox;
				reloadme.reloadAllModels();
			}
			String resultXML = targetBox.processQueryAtUrlAndProduceXml(resolvedQueryURL, optCL);
			logInfo("ResultXML\n-----------------------------------" + resultXML + "\n---------------------------------");
		}
	}

	static public class ReloadSingleModelTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends TriggerImpl<RB> {

		final String graphURI;
		final Reloadable m_repo;

		public ReloadSingleModelTrigger(String graphUri, Reloadable repo) {
			this.graphURI = graphUri;
			m_repo = repo;
		}

		@Override public void fire(RB targetBox) {
			m_repo.reloadSingleModel(graphURI);
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(RepoOper.class);

	@UISalient
	public static boolean inPlaceReplacements;

	@UISalient public static void replaceModelElements(Model dest, Model src) {
		if (src == dest) {
			return;
		}
		dest.removeAll();
		dest.add(src);
		dest.setNsPrefixes(src.getNsPrefixMap());
		// dest.getGraph().getPrefixMapping().equals(obj)
		//if (src.getGraph() )dest.setNsPrefix("", src.getNsPrefixURI(""));
		///dest.setNsPrefix("#", src.getNsPrefixURI("#"));
	}

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
			return getGlobalDShared();
		return createDataset(DATASET_TYPE_DEFAULT_MEMFILE_TYPE);
	}

	@UISalient public static Dataset createDefault() {
		if (alwaysShareDatasetHack)
			return getGlobalDShared();
		return createDataset(DATASET_TYPE_DEFAULT);
	}

	@UISalient public static Dataset createShared() {
		if (alwaysShareDatasetHack)
			return getGlobalDShared();
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

	@UISalient public static void replaceWithDB(Reloadable myRepo, Resource unionOrReplace) {
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = getGlobalDShared();
		myRepo.setMyMainQueryDataset(newDs);
		replaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	@UISalient public static void replaceViaFactory(Reloadable myRepo, UserDatasetFactory factory, Resource unionOrReplace) {
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = factory.create(oldDs);
		myRepo.setMyMainQueryDataset(newDs);
		replaceDatasetElements(newDs, oldDs, unionOrReplace);
	}

	@UISalient public static void replaceWitMemory(Reloadable myRepo, Resource unionOrReplace) {
		Dataset oldDs = myRepo.getMainQueryDataset();
		Dataset newDs = DatasetFactory.createMem();
		myRepo.setMyMainQueryDataset(newDs);
		replaceDatasetElements(newDs, oldDs, unionOrReplace);
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
		if (globalDS == null)
			globalDS = SDBFactory.connectDataset(DemoResources.STORE_CONFIG_PATH);
		return globalDS;
	}

	@UISalient public static void replaceModelElements(Model dest, Model src, Resource unionOrReplace) {
		if (src == dest) {
			return;
		}
		dest.removeAll();
		dest.add(src);
		dest.setNsPrefixes(src.getNsPrefixMap());
		// dest.getGraph().getPrefixMapping().equals(obj)
		//if (src.getGraph() )dest.setNsPrefix("", src.getNsPrefixURI(""));
		///dest.setNsPrefix("#", src.getNsPrefixURI("#"));
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src, String onlyModel) {
		replaceDatasetElements(dest, src, onlyModel, null);
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src, String onlyModel, Resource unionOrReplace) {
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
			return;
		}
		boolean isReplace = isReplace(unionOrReplace);
		Dataset sdest = (Dataset) dest;
		boolean onSrc = true, onDest = true;
		if (!dest.containsNamedModel(onlyModel)) {
			onSrc = false;
			theLogger.warn("Orginal did not contain model" + onlyModel);

		}
		if (!src.containsNamedModel(onlyModel)) {
			onDest = false;
			theLogger.warn("New did not contain model " + onlyModel);
		}
		if (onSrc && onDest) {
			Model destModel = src.getNamedModel(onlyModel);
			Model srcModel = dest.getNamedModel(onlyModel);
			replaceModelElements(destModel, srcModel, unionOrReplace);
			theLogger.info("Replaced model " + onlyModel);
			return;
		}
		if (onSrc) {
			sdest.addNamedModel(onlyModel, src.getNamedModel(onlyModel));
			theLogger.info("Added model " + onlyModel);
			return;
		}
		if (onDest) {
			if (isReplace) {
				dest.getNamedModel(onlyModel).removeAll();
				theLogger.info("clearing model " + onlyModel);
			}
			return;
		}
	}

	public static void readDatasetFromURL(String srcPath, Dataset target, Resource unionOrReplace) throws IOException {
		final Model loaderModel = ModelFactory.createDefaultModel();
		final Dataset loaderDataset = DatasetFactory.createMem();
		Model m = loaderDataset.getDefaultModel();
		if (m == null)
			m = ModelFactory.createDefaultModel();
		final Model[] currentModel = new Model[] { m, null, null };
		final String[] modelName = new String[] { "" };
		final Map<String, Model> constits = new HashMap();
		loaderModel.register(new StatementListener() {

			@Override public void addedStatement(Statement arg0) {
				System.out.println("Adding statement: " + arg0);
				String subjStr = "" + arg0.getSubject();
				if (subjStr.equals("self")) {
					// processing directive
					RDFNode r = arg0.getObject();
					if (r.isLiteral()) {
						// is a model start declaration;
						String baseURI = modelName[0] = r.asLiteral().getString();
						Model newModel = currentModel[0] = ModelFactory.createDefaultModel();
						currentModel[0].setNsPrefix("", baseURI);
					} else if (r.isResource()) {
						// is a model ending declaration (we dont clear)
						Resource rs = r.asResource();
						String type = rs.getLocalName();
						Model newModel = currentModel[0];
						newModel.setNsPrefixes(loaderModel.getNsPrefixMap());
						if (type.equals("DirectoryModel")) {
							currentModel[1] = currentModel[0];
						} else if (type.equals("RepoSheetModel")) {
							constits.put(modelName[0], currentModel[0]);
						} else if (type.equals("DatasetDefaultModel")) {
							currentModel[2] = currentModel[0];
						}
					}
				} else {
					currentModel[0].add(arg0);
				}
			}
		});
		InputStream fis = FileStreamUtils.openInputStream(srcPath, null);
		InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset().name());
		loaderModel.read(isr, null, "TTL");

		if (currentModel[2] != null)
			loaderDataset.setDefaultModel(currentModel[2]);

		for (Map.Entry<String, Model> entry : constits.entrySet()) {
			loaderDataset.addNamedModel(entry.getKey(), entry.getValue());
		}
		replaceDatasetElements(target, loaderDataset, null, unionOrReplace);
	}

	public static void replaceNamedModel(Dataset dest, String urlModel, Model model, Resource unionOrReplace) {
		Lock lock = dest.getLock();
		lock.enterCriticalSection(Lock.WRITE);
		Lock oldLock = null;
		model.enterCriticalSection(Lock.READ);
		try {
			long size = model.size();
			boolean isReplace = isReplace(unionOrReplace);
			boolean onDest = true;
			if (!dest.containsNamedModel(urlModel)) {
				onDest = false;
			}
			if (!onDest) {
				dest.addNamedModel(urlModel, model);
				theLogger.warn("Added new model " + urlModel + " size=" + size);
				return;
			}
			Model old = dest.getNamedModel(urlModel);
			if (old == model) {
				old = null;
				theLogger.warn("Nothing to do.. same model " + urlModel + " size=" + size);
				return;
			}
			oldLock = old.getLock();
			oldLock.enterCriticalSection(Lock.WRITE);
			long sizeBefore = old.size();
			if (RepoOper.inPlaceReplacements) {
				old.removeAll();
				theLogger.warn("In place (Replacing) old model " + urlModel + " size=" + sizeBefore + "-> " + old.size());
				isReplace = false;
			}
			if (!isReplace) {
				old.add(model);
				sizeBefore = old.size();
				old.getNsPrefixMap().putAll(model.getNsPrefixMap());
				long sizeNow = old.size();
				theLogger.warn("Merging into old model " + urlModel + " size(" + sizeBefore + "+" + model.size() + ")->" + sizeNow);
				return;
			}
			theLogger.warn("Replacing old model " + urlModel + " size=" + sizeBefore + "->" + model.size());
			dest.replaceNamedModel(urlModel, model);
		} finally {
			if (oldLock != null)
				oldLock.leaveCriticalSection();

			if (model != null) {
				model.leaveCriticalSection();
			}
			if (lock != null)
				lock.leaveCriticalSection();
		}
	}

	private static boolean isReplace(Resource unionOrReplace) {
		boolean isReplace = true;
		if (unionOrReplace != null) {
			theLogger.warn("Found union/replace = " + unionOrReplace);
			if (unionOrReplace.getLocalName().equals("Union"))
				isReplace = false;
		}
		return isReplace;
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src) {
		replaceDatasetElements(dest, src, (Resource) null);
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src, Resource unionOrReplace) {
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
			return;
		}
		Dataset sdest = (Dataset) dest;
		Model defDestModel = dest.getDefaultModel();
		Model defSrcModel = src.getDefaultModel();
		replaceModelElements(defDestModel, defSrcModel, unionOrReplace);
		HashSet<String> dnames = setOF(sdest.listNames());
		HashSet<String> snames = setOF(src.listNames());
		HashSet<String> replacedModels = new HashSet<String>();
		boolean isReplace = isReplace(unionOrReplace);

		for (String nym : snames) {
			Model getsrc = src.getNamedModel(nym);
			if (dest.containsNamedModel(nym)) {
				Model getdest = dest.getNamedModel(nym);
				replacedModels.add(nym);
				replaceModelElements(getdest, getsrc, unionOrReplace);
				dnames.remove(nym);
				continue;
			}
		}
		for (String nym : replacedModels) {
			snames.remove(nym);
		}

		if (dnames.size() == 0) {
			if (snames.size() == 0) {// perfect!
				return;
			} else {
				// add the new models to the datasource
				for (String nym : snames) {
					sdest.addNamedModel(nym, src.getNamedModel(nym));
				}
				// still good
				return;
			}
		} else {
			// dnames > 0
			if (snames.size() == 0) {
				// some graphs might need cleared?
				for (String nym : dnames) {
					if (isReplace) {
						sdest.getNamedModel(nym).removeAll();
						sdest.removeNamedModel(nym);
					}
				}
				return;
			} else {
				// New names to add AND graphs might need cleared
				for (String nym : dnames) {
					if (isReplace) {
						sdest.getNamedModel(nym).removeAll();
						sdest.removeNamedModel(nym);
					}
				}
				for (String nym : snames) {
					sdest.addNamedModel(nym, src.getNamedModel(nym));
				}
			}
		}
	}

	public static <E> HashSet<E> setOF(Enumeration<E> en) {
		HashSet<E> hs = new HashSet<E>();
		while (en.hasMoreElements()) {
			E e = (E) en.nextElement();
			hs.add(e);
		}
		return hs;
	}

	public static <E> HashSet<E> setOF(Iterator<E> en) {
		HashSet<E> hs = new HashSet<E>();
		while (en.hasNext()) {
			E e = (E) en.next();
			hs.add(e);
		}
		return hs;
	}
}
