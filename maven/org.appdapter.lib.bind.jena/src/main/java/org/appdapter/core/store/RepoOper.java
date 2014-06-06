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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.appdapter.api.trigger.AnyOper;
import org.appdapter.core.debug.UIAnnotations.UIHidden;
import org.appdapter.core.debug.UIAnnotations.UtilClass;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.store.dataset.CheckedGraph;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;
import org.appdapter.demo.DemoResources;
import org.appdapter.fileconv.FileStreamUtils;
import org.appdapter.trigger.bind.jena.TriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.CompositionBase;
import com.hp.hpl.jena.graph.compose.Dyadic;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.Polyadic;
import com.hp.hpl.jena.n3.N3JenaWriterPP;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.vocabulary.RDF;

// import com.hp.hpl.jena.query.DataSource;

/**
 * @author Dmiles
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
@UIHidden
public class RepoOper implements AnyOper, UtilClass {
	@UISalient
	static public interface ISeeToString {
		@Override
		@UISalient(MenuName = "Call ToString")
		public String toString();
	}

	@UISalient
	static public interface ReloadableDataset {

		@UISalient(MenuName = "Reload Repo")
		void reloadAllModels();

		@UISalient()
		void reloadSingleModel(String modelName);

		@UISalient(ToValueMethod = "toString")
		Dataset getMainQueryDataset();

		/**
		 * Causes a repo to replace its mainQueryDataset with the 'ds' param
		 * 
		 * To switch from a file repo to a database repo
		 * 
		 * ReloadableDataset myRepo = new
		 * URLRepoSpec("myturtle.ttl").makeRepo();
		 * 
		 * Dataset old = myRepo.mainQueryDataset();
		 * 
		 * Dataset newDs = SDB.store();
		 * 
		 * 
		 * 
		 * 
		 * @param ds
		 * @return
		 */
		@UISalient(ToValueMethod = "toString")
		void setMyMainQueryDataset(Dataset ds);
	}

	// static class ConcBootstrapTF extends
	// BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
	// } // TT extends TriggerImpl<BT>
	public static class ReloadAllModelsTrigger<RB extends RepoBox<TriggerImpl<RB>>>
			extends TriggerImpl<RB> {

		Repo.WithDirectory m_repo;

		// @TODO obviouly we should be using specs and not repos! but
		// With.Directory may as well be the spec for now.
		// Also consider we are using the actual Repo (not the Spec) due to the
		// fact we must have something to clear and update right?
		public ReloadAllModelsTrigger(Repo.WithDirectory repo) {
			m_repo = repo;
		}

		@Override
		public void fire(RB targetBox) {
			String resolvedQueryURL = DemoResources.QUERY_PATH;
			ClassLoader optCL = getClass().getClassLoader();
			if (targetBox != null) {
				optCL = targetBox.getClass().getClassLoader();
			}
			if (!(m_repo instanceof RepoOper.ReloadableDataset)) {
				theLogger.error("Repo not reloadable! " + targetBox);
			} else {
				RepoOper.ReloadableDataset reloadme = (RepoOper.ReloadableDataset) targetBox;
				reloadme.reloadAllModels();
			}
			String resultXML = targetBox.processQueryAtUrlAndProduceXml(
					resolvedQueryURL, optCL);
			logInfo("ResultXML\n-----------------------------------"
					+ resultXML + "\n---------------------------------");
		}
	}

	static public class ReloadSingleModelTrigger<RB extends RepoBox<TriggerImpl<RB>>>
			extends TriggerImpl<RB> {

		final String graphURI;
		final ReloadableDataset m_repo;

		public ReloadSingleModelTrigger(String graphUri, ReloadableDataset repo) {
			this.graphURI = graphUri;
			m_repo = repo;
		}

		@Override
		public void fire(RB targetBox) {
			m_repo.reloadSingleModel(graphURI);
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(RepoOper.class);

	@UISalient
	public static boolean inPlaceReplacements = false;
	@UISalient(Description = "isMergeDefault means addModelToDataset with some data will add withotu replacing")
	public static boolean isMergeDefault = true;

	@UISalient
	public static void replaceModelElements(Model dest, Model src) {
		addModelElements(dest, src, true);
	}

	@UISalient
	public static void addModelElements(Model dest, Model src,
			boolean clearFirst) {
		if (src == dest) {
			return;
		}

		if (clearFirst)
			dest.removeAll();
		dest.add(src);
		dest.setNsPrefixes(src.getNsPrefixMap());
		// dest.getGraph().getPrefixMapping().equals(obj)
		// if (src.getGraph() )dest.setNsPrefix("", src.getNsPrefixURI(""));
		// /dest.setNsPrefix("#", src.getNsPrefixURI("#"));
	}

	public static Model unionAll(Dataset ds, Model... more) {
		Graph[] gs = getAllGraphs(ds, more);
		Model m = ModelFactory.createModelForGraph(new MultiUnion(gs));
		return m;
	}

	public static void addUnionModel(Dataset ds, String src, String dest) {
		src = correctModelName(src);
		dest = correctModelName(dest);

		Model destM = ds.getNamedModel(dest);
		Model srcM = ds.getNamedModel(src);
		if (destM == srcM)
			return;
		if (srcM == null) {
			srcM = RepoDatasetFactory.findOrCreateModel(ds, src);
			// throw new RuntimeException("Missing Model named: " + srcM);
		}
		if (destM == null) {
			destM = RepoDatasetFactory.createGroup(srcM);
			ds.addNamedModel(dest, destM);
		} else {
			destM = RepoDatasetFactory.createGroup(destM, srcM);
			theLoggerInfo("Made Merged Model from " + src + " and " + dest);
			ds.replaceNamedModel(dest, destM);
		}
	}

	public static void replaceSingleDatasetModel(Dataset dest, Dataset src,
			String onlyModel) {
		onlyModel = correctModelName(onlyModel);
		putSingleOrAllDatasetModel(dest, src, onlyModel, true);
	}

	public static String correctModelName(String onlyModel) {
		return RepoDatasetFactory.correctModelName(onlyModel).getURI();
	}

	public static void putSingleDatasetModel(Dataset dest, Dataset src,
			String onlyModel, Resource unionOrReplace) {
		putSingleOrAllDatasetModel(dest, src, onlyModel,
				isReplace(unionOrReplace));
	}

	public static void putAllDatasetModels(Dataset dest, Dataset src,
			Resource unionOrReplace) {
		putSingleOrAllDatasetModel(dest, src, null, isReplace(unionOrReplace));
	}

	private static void putSingleOrAllDatasetModel(Dataset dest, Dataset src,
			String onlyModel, boolean isReplace) {
		if (onlyModel == null) {
			Debuggable.notImplemented("putAllDatasetModels...");
		}
		onlyModel = correctModelName(onlyModel);
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! "
					+ dest.getClass() + " " + dest);
			return;
		}
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
			addModelElements(destModel, srcModel, isReplace);
			theLoggerInfo("Replaced model " + onlyModel);
			return;
		}
		if (onSrc) {
			sdest.addNamedModel(onlyModel, src.getNamedModel(onlyModel));
			theLoggerInfo("Added model " + onlyModel);
			return;
		}
		if (onDest) {
			if (isReplace) {
				dest.getNamedModel(onlyModel).removeAll();
				theLoggerInfo("clearing model " + onlyModel);
			}
			return;
		}
	}

	public static void readDatasetFromURL(String srcPath, Dataset target,
			Resource unionOrReplace) throws IOException {
		final Model loaderModel = RepoDatasetFactory.createPrivateMemModel();
		final Dataset loaderDataset = DatasetFactory.createMem();
		Model m = loaderDataset.getDefaultModel();
		if (m == null) {
			m = RepoDatasetFactory.createPrivateMemModel();
			loaderDataset.setDefaultModel(m);
		}
		final Model[] currentModel = new Model[] { m, null, null };
		final String[] modelName = new String[] { "" };
		final Map<String, Model> constits = new HashMap();
		loaderModel.register(new StatementListener() {

			@Override
			public void addedStatement(Statement arg0) {
				System.out.println("Adding statement: " + arg0);
				String subjStr = "" + arg0.getSubject();
				if (subjStr.equals("self")) {
					// processing directive
					RDFNode r = arg0.getObject();
					if (r.isLiteral()) {
						// is a model start declaration;
						String baseURI = modelName[0] = r.asLiteral()
								.getString();
						currentModel[0] = RepoDatasetFactory
								.createPrivateMemModel();
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
		FileStreamUtils fus = new ExtendedFileStreamUtils();
		InputStream fis = fus.openInputStream(srcPath, null);
		InputStreamReader isr = new InputStreamReader(fis, Charset
				.defaultCharset().name());
		loaderModel.read(isr, null, "TTL");

		if (currentModel[2] != null)
			loaderDataset.setDefaultModel(currentModel[2]);

		for (Map.Entry<String, Model> entry : constits.entrySet()) {
			loaderDataset.addNamedModel(entry.getKey(), entry.getValue());
		}
		putAllDatasetModels(target, loaderDataset, unionOrReplace);
	}

	public static void putNamedModel(Dataset dest, String urlModel,
			Model model, Resource unionOrReplace) {
		boolean isReplace = isReplace(unionOrReplace);
		urlModel = RepoOper.correctModelName(urlModel);
		Lock lock = dest.getLock();
		lock.enterCriticalSection(Lock.WRITE);
		Lock oldLock = null;
		model.enterCriticalSection(Lock.READ);

		try {
			long size = model.size();
			boolean onDest = true;
			if (!dest.containsNamedModel(urlModel)) {
				onDest = false;
			}
			if (!onDest) {
				dest.addNamedModel(urlModel, model);
				theLoggerInfo("Added new model " + urlModel + " size=" + size);
				return;
			}
			Model old = dest.getNamedModel(urlModel);
			if (old == model) {
				theLoggerInfo("Nothing to do.. same model " + urlModel
						+ " size=" + size);
				return;
			}
			oldLock = old.getLock();
			oldLock.enterCriticalSection(Lock.WRITE);
			long sizeBefore = old.size();
			if (RepoOper.inPlaceReplacements) {
				old.removeAll();
				theLoggerInfo("In place (Replacing) old model " + urlModel
						+ " size=" + sizeBefore + "-> " + old.size());
				isReplace = false;
			}
			if (!isReplace) {
				sizeBefore = old.size();
				old.add(model);
				if (!old.samePrefixMappingAs(model)) {
					old.withDefaultMappings(model);
				}
				long sizeNow = old.size();
				theLoggerInfo("Merging into old model " + urlModel + " size("
						+ sizeBefore + "+" + model.size() + ")->" + sizeNow);
				RepoDatasetFactory.invalidateModel(model);
				return;
			}
			RepoDatasetFactory.invalidateModel(old);
			theLoggerInfo("Replacing old model " + urlModel + " size="
					+ sizeBefore + "->" + model.size());
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

	private static void theLoggerInfo(String string) {
		System.out.println("RepoOper: " + string);

	}

	public static void clearAll(Dataset ds) {
		Model dModel = ds.getDefaultModel();
		removeAll(dModel);
		Iterator<String> sIterator = ds.listNames();
		while (sIterator.hasNext()) {
			String mName = sIterator.next();
			Model model = ds.getNamedModel(mName);
			removeAll(model);
		}
	}

	private static void removeAll(Model model) {
		if (model == null || model.size() == 0)
			return;
		model.removeAll();
	}

	private static boolean isReplace(Resource unionOrReplace) {
		boolean isReplace = !isMergeDefault;
		if (unionOrReplace != null) {
			theLogger.warn("Found union/replace = " + unionOrReplace);
			if (unionOrReplace.getLocalName().equals("Union"))
				isReplace = false;
		}
		return isReplace;
	}

	public static void replaceDatasetElements(Dataset dest, Dataset src) {
		addOrReplaceDatasetElements(dest, src, (Resource) null);
	}

	public static void addOrReplaceDatasetElements(Dataset dest, Dataset src,
			Resource unionOrReplace) {
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! "
					+ dest.getClass() + " " + dest);
			return;
		}
		Dataset sdest = (Dataset) dest;
		Model defDestModel = dest.getDefaultModel();
		Model defSrcModel = src.getDefaultModel();
		boolean isReplace = isReplace(unionOrReplace);

		addModelElements(defDestModel, defSrcModel, isReplace);

		HashSet<String> dnames = setOF(sdest.listNames());
		HashSet<String> snames = setOF(src.listNames());
		HashSet<String> replacedModels = new HashSet<String>();

		for (String nym : snames) {
			Model getsrc = src.getNamedModel(nym);
			if (dest.containsNamedModel(nym)) {
				Model getdest = dest.getNamedModel(nym);
				replacedModels.add(nym);
				addModelElements(getdest, getsrc, isReplace);
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

	public static void registerDatasetFactory(String datasetTypeName,
			UserDatasetFactory factory) {
		RepoDatasetFactory.registerDatasetFactory(datasetTypeName, factory);
	}

	public static void writeRepoToDirectory(Repo repo, String dir,
			boolean solidifyDerivedModels) throws IOException {
		if (!(repo instanceof Repo.WithDirectory)) {
			System.out.println("Not Repo.WithDirectory  " + repo.getClass()
					+ " " + repo);
			return;
		}
		Dataset ds = repo.getMainQueryDataset();
		Model dirModel = ((Repo.WithDirectory) repo).getDirectoryModel();
		String csiURI = dirModel.getNsPrefixURI("csi");
		new File(dir).mkdir();

		FileWriter fw = null;
		if (false) {
			try {
				fw = new FileWriter(new File(new File(dir), "all.trig"));
				writeTriG(repo, fw);
			} catch (IOException io) {

			} finally {
				if (fw != null)
					fw.close();
			}
		}
		String rname = new SimpleDateFormat("yyyyMMddHH_mmss_SSS")
				.format(new Date());
		Node fileRepoName = dirModel.getResource(csiURI + "filerepo_" + rname)
				.asNode();
		Map<String, String> nsUsed = new HashMap<String, String>();
		RepoOper.saveRepoAsManyTTLs(fileRepoName, dir, nsUsed, dirModel, ds,
				false, solidifyDerivedModels);

	}

	public static void saveRepoAsManyTTLs(Node fileRepoName, String dir,
			Map<String, String> nsUsed, Model dirModel, Dataset ds,
			boolean dontChangeDirModel, boolean solidifyDerivedModels)
			throws IOException {
		new File(dir).mkdir();

		PrefixMappingImpl pm = new PrefixMappingImpl();
		pm.setNsPrefixes(nsUsed);
		pm.withDefaultMappings(PrefixMappingImpl.Extended);

		String ccrtNS = dirModel.getNsPrefixURI("ccrt");
		// String frtURI = dirModel.getNsPrefixURI("frt");
		Node fileRepo = dirModel.getResource(ccrtNS + "FileRepo").asNode();
		Node fileModel = dirModel.getResource(ccrtNS + "FileModel").asNode();
		Set<Node> sheetTypes = new HashSet<Node>();
		sheetTypes.add(fileModel);
		sheetTypes.add(dirModel.getResource(ccrtNS + "GoogSheet").asNode());
		sheetTypes.add(dirModel.getResource(ccrtNS + "XlsxSheet").asNode());
		sheetTypes.add(dirModel.getResource(ccrtNS + "CsvFileSheet").asNode());
		Set<Node> sheetTypesToLocalize = new HashSet<Node>();
		sheetTypesToLocalize.addAll(sheetTypes);
		sheetTypesToLocalize.add(fileModel);
		Node repo = dirModel.createProperty(ccrtNS, "repo").asNode();
		Node rdftype = RDF.type.asNode();
		Node sourcePath = dirModel.createProperty(ccrtNS, "sourcePath")
				.asNode();

		DatasetGraph dsg = ds.asDatasetGraph();

		Model defaultModel = ds.getDefaultModel();
		Node defaultURI = null;
		pm.withDefaultMappings(dirModel);

		Graph dirqGraph = dirModel.getGraph();
		{
			// letting dir models advertise all namespaces
			File file = new File(dir, dontChangeDirModel ? "dir.ttl"
					: "dir.old");
			file.getParentFile().mkdirs();
			PrintWriter ow = new PrintWriter(file);
			ow.println("\n");
			writeModel(dirModel, ow, true, false, pm);
			ow.println("\n");
			ow.println("# modelSize=" + dirModel.size() + "\n\n");
			ow.close();
		}
		Graph newGraph = GraphFactory.createGraphMem();

		newGraph.add(new Triple(fileRepoName, sourcePath, NodeFactory
				.createLiteral(dir)));
		newGraph.add(new Triple(fileRepoName, rdftype, fileRepo));

		ArrayList<Node> derived = new ArrayList<Node>();
		ArrayList<Node> sourceOf = new ArrayList<Node>();
		ArrayList<Node> allNodes = new ArrayList<Node>();

		Iterator<Node> dni = dsg.listGraphNodes();
		while (dni.hasNext()) {
			Node gname = dni.next();
			String nodeName = gname.toString();
			if (nodeName == null || nodeName.equals("#all")
					|| nodeName.startsWith("#"))
				continue;
			allNodes.add(gname);
			Model m = ds.getNamedModel(gname.toString());
			nsUsed.putAll(m.getNsPrefixMap());
			pm.withDefaultMappings(m);
			if (m == defaultModel) {
				defaultURI = gname;
			}
			if (isDerivedModel(m, ds)) {
				derived.add(gname);
				ArrayList<Node> named = new ArrayList<Node>();
				derivedFromModels(m, dsg, null, null, named);
				for (Node s : named) {
					sourceOf.add(s);
				}
			}
		}

		dni = ds.asDatasetGraph().listGraphNodes();

		boolean skipMergedModels = solidifyDerivedModels;

		while (dni.hasNext()) {
			Node gname = dni.next();
			String nodeName = gname.toString();
			if (nodeName == null || nodeName.equals("#all")
					|| nodeName.startsWith("#"))
				continue;
			String name = gname.getLocalName();
			String filename = name + ".ttl";
			boolean addToDirModel = true;
			if (derived.contains(gname)) {
				if (!solidifyDerivedModels) {
					filename = name + ".pipe_dest";
					addToDirModel = false;
				}
			}
			if (sourceOf.contains(gname)) {
				if (skipMergedModels) {
					filename = name + ".pipe_src";
					addToDirModel = false;
				}
			}

			Model m = ds.getNamedModel(gname.toString());
			for (Triple was : dirqGraph.find(gname, rdftype, Node.ANY).toList()) {
				if (!sheetTypes.contains(was.getObject())) {
					newGraph.add(was);
				}
				// NodeIterator foo =
				// dirModel.listObjectsOfProperty(dirModel.createResource(gname.getURI()),
				// dirModel.createProperty(sourcePath.getURI()));
				if (addToDirModel) {
					newGraph.add(new Triple(gname, repo, fileRepoName));
					newGraph.add(new Triple(gname, rdftype, fileModel));
					newGraph.add(new Triple(gname, sourcePath, NodeFactory
							.createLiteral(filename)));
				}
			}

			PrintWriter ow = new PrintWriter(new File(dir, filename));
			ow.println("# modelName=" + gname);
			if (m == defaultModel) {
				defaultURI = gname;
				ow.println("# isDefaultModel\n");
			}
			ow.println("# modelSize=" + m.size() + "\n\n");
			if (derived.contains(gname)) {
				ow.println("# derivedModel \n");
				ArrayList<Node> named = new ArrayList<Node>();
				derivedFromModels(m, dsg, null, null, named);
				for (Node s : named) {
					ow.println("#  contains " + s + "");
				}
			}
			ow.println("\n");
			writeModel(m, ow, true, true, pm);
			ow.println("\n");

			ow.close();
		}

		if (dirModel != null && !dontChangeDirModel) {
			File file = new File(dir, "dir.ttl");
			PrintWriter ow = new PrintWriter(file);
			ow.println("# load this with..  Repo repo = new UrlRepoSpec(\""
					+ file.toURL() + "\").makeRepo();\n");
			ow.println("\n");
			Model m = RepoDatasetFactory.createPrivateMemModel();
			m.setNsPrefixes(dirModel);
			m.add(ModelFactory.createModelForGraph(newGraph));
			writeModel(m, ow, true, false, pm);
			ow.println("\n");
			ow.println("# modelSize=" + dirModel.size() + "\n\n");
			ow.println("# dirModel = " + dirModel.size());
			if (defaultURI != null) {
				ow.println("# defaultModel = " + defaultURI);
			}
			ow.close();
		}
	}

	private static boolean isDerivedModel(Model m, Dataset ds) {
		Graph g = getUnderlyingGraph(m.getGraph());
		if (g instanceof CompositionBase) {
			return true;
		}
		return false;
	}

	private static void derivedFromModels(Model m0, DatasetGraph ds,
			List<Graph> ums, List<Object> list, List<Node> named) {
		if (list == null)
			list = new ArrayList<Object>();
		Graph m = getUnderlyingGraph(m0.getGraph());
		addConstituentGraphs(m, list);
		list.remove(m);
		if (list.size() == 0) {
			return;
		}
		Iterator<Node> ims = ds.listGraphNodes();
		while (ims.hasNext()) {
			Node name = ims.next();
			if (named != null && named.contains(name))
				continue;
			Graph min = getUnderlyingGraph(ds.getGraph(name));
			if (m == min)
				continue;
			if (ums != null && ums.contains(min))
				continue;

			if (list.contains(getUnderlyingGraph(min))) {
				if (ums != null)
					ums.add(min);
				if (named != null)
					named.add(name);
			}
		}
	}

	public static void addConstituentGraphs(Graph g, Collection list) {
		addConstituentGraphs(g, list, false);
	}

	public static void addConstituentGraphs(Graph g, Collection list,
			boolean retainMultis) {
		if (g == null || list.contains(g))
			return;
		boolean retainG = false;
		try {
			list.add(g);
			if (g instanceof Dyadic) {
				addConstituentGraphs((Graph) ((Dyadic) g).getL(), list);
				addConstituentGraphs((Graph) ((Dyadic) g).getR(), list);
				if (retainMultis) {
					retainG = true;
				}
				return;
			}
			if (g instanceof Polyadic) {
				for (Graph g0 : ((Polyadic) g).getSubGraphs()) {
					addConstituentGraphs(g0, list);
				}
				if (retainMultis) {
					retainG = true;
				}
				return;
			}

			Graph ug = getUnderlyingGraph((Graph) g);
			if (ug != g) {
				addConstituentGraphs(ug, list);
			} else {
				retainG = true;
			}
		} finally {
			if (!retainG)
				list.remove(g);
		}

	}

	public static void writeModel(Model m, Writer ow,
			boolean includeNamespaces, boolean trimNamepaces, PrefixMapping pm)
			throws IOException {
		Graph graph = getUnderlyingGraph(m.getGraph());
		if (graph instanceof CompositionBase) {
			ow.write("# CompositionBase = " + graph.getClass() + "\n");
		}
		Model m2 = ModelFactory.createDefaultModel();
		List<Statement> stmts = m.listStatements().toList();
		HashMap<String, RDFNode> usedNS = new HashMap<String, RDFNode>();
		for (Statement stmt : stmts) {
			addNamespace(usedNS, stmt.getSubject());
			addNamespace(usedNS, stmt.getPredicate());
			addNamespace(usedNS, stmt.getObject());
		}
		int size0 = usedNS.size();
		for (RDFNode s : m.listSubjects().toSet()) {
			addNamespace(usedNS, s);
		}
		for (RDFNode s : m.listObjects().toSet()) {
			addNamespace(usedNS, s);
		}
		int size1 = usedNS.size();
		if (size0 != size1) {
			Debuggable.oldBug(ow, "# nssize0 = " + size0 + "\n");
			Debuggable.oldBug(ow, "# nssize1 = " + size1 + "\n");
		}
		for (String ns : usedNS.keySet()) {
			String prefix = m.getNsURIPrefix(ns);
			String why = "" + usedNS.get(ns);
			if (prefix == null) {
				prefix = pm.getNsURIPrefix(ns);
			}
			if (prefix == null) {
				if (ns.endsWith(":")) {
					Debuggable.oldBug(ow, "# ODD ns namespace ref " + ns
							+ " caused by " + why + " writing " + ow);
					prefix = ns.substring(0, ns.length() - 1);
					ns = pm.getNsURIPrefix(prefix);
				}
			}
			if (ns == null || prefix == null) {
				Debuggable.oldBug(ow, "# missing namespace ref for " + prefix
						+ "=" + ns + " caused by " + why);
				continue;
			}
			m2.setNsPrefix(prefix, ns);
		}
		m2.add(m);
		if (!trimNamepaces) {
			m2.withDefaultMappings(m);
		}
		N3JenaWriterPP jenaWriter = new RDFSortedWriter(includeNamespaces);
		jenaWriter.write(m2, ow, "TTL");
	}

	private static void addNamespace(HashMap<String, RDFNode> usedNS, RDFNode n) {
		String ns;
		if (n == null)
			return;
		if (n.isURIResource()) {
			ns = n.asResource().getNameSpace();
			if (ns != null) {
				usedNS.put(ns, n);
			}
		}
		if (n.isLiteral()) {
			if (n.asLiteral().getDatatype() != null) {
				usedNS.put(XSDDatatype.XSD + "#", n);
			}

		}
	}

	public static Graph getUnderlyingGraph(Graph graph) {
		while (graph instanceof CheckedGraph) {
			graph = ((CheckedGraph) graph).getDataGraph();
		}
		return graph;
	}

	public static void writeTriG(Repo boundRepo, Writer ow) throws IOException {

		Dataset ds = boundRepo.getMainQueryDataset();
		Dataset datasetw = DatasetFactory.create(ds);
		Model defm = ds.getDefaultModel();

		Model dirModel = null;
		if (boundRepo instanceof Repo.WithDirectory) {
			dirModel = ((Repo.WithDirectory) boundRepo).getDirectoryModel();
		}
		if (dirModel != null) {
			datasetw.addNamedModel("#dir", dirModel);
		}

		RDFDataMgr.write(ow, datasetw.asDatasetGraph(), RDFFormat.TRIG);
	}

	public static String getBaseURI(Model defaultModel, String name) {
		String baseURI = defaultModel.getNsPrefixURI("");
		if (baseURI == null) {
			baseURI = name;
		}
		if (baseURI == null)
			baseURI = "http://modelToOntoModel/modelToOntoModel_model_"
					+ System.identityHashCode(defaultModel) + "#";
		return baseURI;
	}

	public static String getModelSource(Model boundModel) {
		// Serialize model and update text area
		StringWriter writer = new StringWriter();
		// xferPrefixes(boundModel, null);
		Map<String, String> pmap = boundModel.getNsPrefixMap();
		boundModel.write(writer, "TTL");
		String turtle = writer.toString();
		Iterator it = pmap.keySet().iterator();
		while (it.hasNext()) {
			String prefix = (String) it.next();
			String uri = pmap.get(prefix);
			if (prefix.length() > 0) {
				String remove = "\\@prefix " + prefix + "\\:.*\\<" + uri
						+ "\\> .\n";
				turtle = turtle.replaceAll(remove, "");
			}
		}
		return turtle;
	}

	static public Model loadTTLReturnDirModel(final Dataset targetDataset,
			InputStream fis) {
		RDFDataMgr.read(targetDataset, fis, Lang.TRIG);
		Model dirModel = targetDataset.getNamedModel("#dir");
		if (dirModel == null) {
			dirModel = targetDataset.getDefaultModel();
		}
		return dirModel;
	}

	protected static Model findOrCreate(Dataset targetDataset, String baseURI,
			final PrefixMapping nsMap) {
		if (targetDataset.containsNamedModel(baseURI)) {
			return targetDataset.getNamedModel(baseURI);
		}
		Model newModel = RepoDatasetFactory.createPrivateMemModel();
		targetDataset.addNamedModel(baseURI, newModel);
		newModel.setNsPrefixes(nsMap);
		newModel.setNsPrefix("", baseURI);
		return newModel;
	}

	public static Model makeReadOnly(Model model) {
		return model;
	}

	static public Graph[] getAllGraphs(Dataset ds, Model... more) {
		Iterator<String> names = ds.listNames();
		Collection<Graph> grpGraph = new HashSet<Graph>();
		while (names.hasNext()) {
			String nodeName = names.next();
			if (nodeName == null || nodeName.equals("#all")
					|| nodeName.startsWith("#"))
				continue;
			Model m = ds.getNamedModel(nodeName);
			addConstituentGraphs(RepoOper.getUnderlyingGraph(m.getGraph()),
					grpGraph);
		}
		for (Model dm : more) {
			if (dm != null) {
				addConstituentGraphs(
						RepoOper.getUnderlyingGraph(dm.getGraph()), grpGraph);
			}
		}
		Graph[] gs = grpGraph.toArray(new Graph[grpGraph.size()]);
		return gs;
	}
}
