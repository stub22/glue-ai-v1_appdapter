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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import org.appdapter.core.matdat.OnlineSheetRepoSpec;
import org.appdapter.core.matdat.RepoSpec;
import org.appdapter.core.matdat.URLRepoSpec;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;
import org.appdapter.demo.DemoResources;
import org.appdapter.fileconv.FileStreamUtils;
import org.appdapter.trigger.bind.jena.TriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
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
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp;
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
		@Override @UISalient(MenuName = "Call ToString") public String toString();
	}

	@UISalient
	static public interface ReloadableDataset {

		@UISalient(MenuName = "Reload Repo") void reloadAllModels();

		@UISalient() void reloadSingleModel(String modelName);

		@UISalient(ToValueMethod = "toString") Dataset getMainQueryDataset();

		/**
		 * Causes a repo to replace its mainQueryDataset with the 'ds' param
		 * 
		 * To switch from a file repo to a database repo
		 * 
		 * ReloadableDataset myRepo = new URLRepoSpec("myturtle.ttl").makeRepo();
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
			if (!(m_repo instanceof RepoOper.ReloadableDataset)) {
				theLogger.error("Repo not reloadable! " + targetBox);
			} else {
				RepoOper.ReloadableDataset reloadme = (RepoOper.ReloadableDataset) targetBox;
				reloadme.reloadAllModels();
			}
			String resultXML = targetBox.processQueryAtUrlAndProduceXml(resolvedQueryURL, optCL);
			logInfo("ResultXML\n-----------------------------------" + resultXML + "\n---------------------------------");
		}
	}

	static public class ReloadSingleModelTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends TriggerImpl<RB> {

		final String graphURI;
		final ReloadableDataset m_repo;

		public ReloadSingleModelTrigger(String graphUri, ReloadableDataset repo) {
			this.graphURI = graphUri;
			m_repo = repo;
		}

		@Override public void fire(RB targetBox) {
			m_repo.reloadSingleModel(graphURI);
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(RepoOper.class);

	@UISalient
	public static boolean inPlaceReplacements = false;
	@UISalient(Description = "isMergeDefault means addModelToDataset with some data will add withotu replacing")
	public static boolean isMergeDefault = true;

	@UISalient public static void replaceModelElements(Model dest, Model src) {
		addModelElements(dest, src, true);
	}

	@UISalient public static void addModelElements(Model dest, Model src, boolean clearFirst) {
		if (src == dest) {
			return;
		}

		if (clearFirst)
			dest.removeAll();
		dest.add(src);
		dest.setNsPrefixes(src.getNsPrefixMap());
		// dest.getGraph().getPrefixMapping().equals(obj)
		//if (src.getGraph() )dest.setNsPrefix("", src.getNsPrefixURI(""));
		///dest.setNsPrefix("#", src.getNsPrefixURI("#"));
	}

	public static void addUnionModel(Dataset ds, String src, String dest) {
		src = correctModelName(src);
		dest = correctModelName(dest);
		Model destM = ds.getNamedModel(dest);
		Model srcM = ds.getNamedModel(src);
		if (srcM == null) {
			throw new RuntimeException("Missing Model named: " + srcM);
		}
		if (destM == null) {
			destM = srcM;
			ds.addNamedModel(dest, destM);
		} else {
			destM = RepoDatasetFactory.createUnion(destM, srcM);
			ds.replaceNamedModel(dest, destM);
			theLogger.warn("Made Merged Model from " + src);
		}
	}

	public static void replaceSingleDatasetModel(Dataset dest, Dataset src, String onlyModel) {
		onlyModel = correctModelName(onlyModel);
		putSingleOrAllDatasetModel(dest, src, onlyModel, true);
	}

	public static String correctModelName(String onlyModel) {
		return RepoDatasetFactory.correctModelName(onlyModel).getURI();
	}

	public static void putSingleDatasetModel(Dataset dest, Dataset src, String onlyModel, Resource unionOrReplace) {
		putSingleOrAllDatasetModel(dest, src, onlyModel, isReplace(unionOrReplace));
	}

	public static void putAllDatasetModels(Dataset dest, Dataset src, Resource unionOrReplace) {
		putSingleOrAllDatasetModel(dest, src, null, isReplace(unionOrReplace));
	}

	private static void putSingleOrAllDatasetModel(Dataset dest, Dataset src, String onlyModel, boolean isReplace) {
		if (onlyModel == null) {
			Debuggable.notImplemented("putAllDatasetModels...");
		}
		onlyModel = correctModelName(onlyModel);
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
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
		final Model loaderModel = RepoDatasetFactory.createDefaultModelUnshared();
		final Dataset loaderDataset = DatasetFactory.createMem();
		Model m = loaderDataset.getDefaultModel();
		if (m == null) {
			m = RepoDatasetFactory.createDefaultModelUnshared();
			loaderDataset.setDefaultModel(m);
		}
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
						currentModel[0] = RepoDatasetFactory.createDefaultModelUnshared();
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
		InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset().name());
		loaderModel.read(isr, null, "TTL");

		if (currentModel[2] != null)
			loaderDataset.setDefaultModel(currentModel[2]);

		for (Map.Entry<String, Model> entry : constits.entrySet()) {
			loaderDataset.addNamedModel(entry.getKey(), entry.getValue());
		}
		putAllDatasetModels(target, loaderDataset, unionOrReplace);
	}

	public static void putNamedModel(Dataset dest, String urlModel, Model model, Resource unionOrReplace) {
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
				theLogger.warn("Added new model " + urlModel + " size=" + size);
				return;
			}
			Model old = dest.getNamedModel(urlModel);
			if (old == model) {
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
				sizeBefore = old.size();
				old.add(model);
				if (!old.samePrefixMappingAs(model)) {
					old.getNsPrefixMap().putAll(model.getNsPrefixMap());
				}
				long sizeNow = old.size();
				theLogger.warn("Merging into old model " + urlModel + " size(" + sizeBefore + "+" + model.size() + ")->" + sizeNow);
				RepoDatasetFactory.invalidateModel(old);
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

	public static void addOrReplaceDatasetElements(Dataset dest, Dataset src, Resource unionOrReplace) {
		if (!(dest instanceof Dataset)) {
			theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
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

	public static void registerDatasetFactory(String datasetTypeName, UserDatasetFactory factory) {
		RepoDatasetFactory.registerDatasetFactory(datasetTypeName, factory);
	}

	public static void writeRepoToDirectory(Repo repo, String dir) throws IOException {
		if (!(repo instanceof Repo.WithDirectory)) {
			System.out.println("Not Repo.WithDirectory  " + repo.getClass() + " " + repo);
			return;
		}
		Dataset ds = repo.getMainQueryDataset();
		Model dirModel = ((Repo.WithDirectory) repo).getDirectoryModel();
		String csiURI = dirModel.getNsPrefixURI("csi");
		String rname = new SimpleDateFormat("yyyyMMddHH_mmss_SSS").format(new Date());
		Node fileRepoName = dirModel.getResource(csiURI + "filerepo_" + rname).asNode();
		RepoOper.saveRepoAsManyTTLs(fileRepoName, dir, dirModel, ds, false);

	}

	public static void saveRepoAsManyTTLs(Node fileRepoName, String dir, Model dirModel, Dataset ds, boolean dontChangeDir) throws IOException {
		new File(dir).mkdir();

		String ccrtNS = dirModel.getNsPrefixURI("ccrt");
		String frtURI = dirModel.getNsPrefixURI("frt");
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
		Node repo = dirModel.createProperty(frtURI, "repo").asNode();
		Node rdftype = RDF.type.asNode();
		Node sourcePath = dirModel.createProperty(frtURI, "sourcePath").asNode();

		Iterator<Node> dni = ds.asDatasetGraph().listGraphNodes();
		Model defaultModel = ds.getDefaultModel();
		Node defaultURI = null;

		Graph dirqGraph = dirModel.getGraph();
		{
			// letting dir models advertise all namespaces
			File file = new File(dir, dontChangeDir ? "dir.ttl" : "dir.old");
			PrintWriter ow = new PrintWriter(file);
			ow.println("\n");
			writeModel(dirModel, ow, true);
			ow.println("\n");
			ow.println("# modelSize=" + dirModel.size() + "\n\n");
			ow.close();
		}
		Graph newGraph = GraphFactory.createGraphMem();

		newGraph.add(new Triple(fileRepoName, sourcePath, NodeFactory.createLiteral(dir)));
		newGraph.add(new Triple(fileRepoName, rdftype, fileRepo));
		while (dni.hasNext()) {
			Node gname = dni.next();
			String name = gname.getLocalName();
			String filename = name + ".ttl";
			Model m = ds.getNamedModel(gname.toString());
			for (Triple was : dirqGraph.find(gname, rdftype, Node.ANY).toList()) {
				if (!sheetTypes.contains(was.getObject())) {
					newGraph.add(was);
				}
				//NodeIterator foo = 
				// dirModel.listObjectsOfProperty(dirModel.createResource(gname.getURI()), dirModel.createProperty(sourcePath.getURI()));
				newGraph.add(new Triple(gname, repo, fileRepoName));
				newGraph.add(new Triple(gname, rdftype, fileModel));
				newGraph.add(new Triple(gname, sourcePath, NodeFactory.createLiteral(filename)));
			}

			PrintWriter ow = new PrintWriter(new File(dir, filename));
			ow.println("\n");
			writeModel(m, ow, true);
			ow.println("\n");
			ow.println("# modelName=" + gname);
			ow.println("# modelSize=" + m.size() + "\n\n");
			if (m == defaultModel) {
				defaultURI = gname;
				ow.println("# isDefaultModel\n");
			}

			ow.close();
		}

		if (dirModel != null && !dontChangeDir) {
			File file = new File(dir, "dir.ttl");
			PrintWriter ow = new PrintWriter(file);
			ow.println("# load this with..  Repo repo = new UrlRepoSpec(\"" + file.toURL() + "\").makeRepo();\n");
			ow.println("\n");
			Model m = RepoDatasetFactory.createDefaultModelUnshared();
			m.setNsPrefixes(dirModel);
			m.add(ModelFactory.createModelForGraph(newGraph));
			writeModel(m, ow, true);
			ow.println("\n");
			ow.println("# modelSize=" + dirModel.size() + "\n\n");
			ow.println("# dirModel = " + dirModel.size());
			if (defaultURI != null) {
				ow.println("# defaultModel = " + defaultURI);
			}
			ow.close();
		}
	}

	public static void writeModel(Model m, Writer ow, boolean includeNamespaces) {
		Model m2 = RepoDatasetFactory.createDefaultModelUnshared();
		List<Statement> stmts = m.listStatements().toList();
		Set<String> usedNS = new HashSet<String>(m.listNameSpaces().toList());

		for (Statement stmt : stmts) {
			String ns = stmt.getSubject().getNameSpace();
			if (ns != null) {
				usedNS.add(ns);
			}
		}
		for (String ns : usedNS) {
			String prefix = m.getNsURIPrefix(ns);
			//if (prefix == null)
			//	prefix = pm.getNsURIPrefix(ns);
			if (prefix == null) {
				continue;
			}
			m2.setNsPrefix(prefix, ns);
		}
		m2.add(m);
		N3JenaWriterPP jenaWriter = new RDFSortedWriter(includeNamespaces);
		jenaWriter.write(m, ow, "TTL");
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
			baseURI = "http://modelToOntoModel/modelToOntoModel_model_" + System.identityHashCode(defaultModel) + "#";
		return baseURI;
	}

	public static String getModelSource(Model boundModel) {
		// Serialize model and update text area
		StringWriter writer = new StringWriter();
		//xferPrefixes(boundModel, null);
		Map<String, String> pmap = boundModel.getNsPrefixMap();
		boundModel.write(writer, "TTL");
		String turtle = writer.toString();
		Iterator it = pmap.keySet().iterator();
		while (it.hasNext()) {
			String prefix = (String) it.next();
			String uri = pmap.get(prefix);
			if (prefix.length() > 0) {
				String remove = "\\@prefix " + prefix + "\\:.*\\<" + uri + "\\> .\n";
				turtle = turtle.replaceAll(remove, "");
			}
		}
		return turtle;
	}

	static public Model loadTTLReturnDirModel(final Dataset targetDataset, InputStream fis) {
		RDFDataMgr.read(targetDataset, fis, Lang.TRIG);
		Model dirModel = targetDataset.getNamedModel("#dir");
		if (dirModel == null) {
			dirModel = targetDataset.getDefaultModel();
		}
		return dirModel;
	}

	protected static Model findOrCreate(Dataset targetDataset, String baseURI, final PrefixMapping nsMap) {
		if (targetDataset.containsNamedModel(baseURI)) {
			return targetDataset.getNamedModel(baseURI);
		}
		Model newModel = RepoDatasetFactory.createDefaultModelUnshared();
		targetDataset.addNamedModel(baseURI, newModel);
		newModel.setNsPrefixes(nsMap);
		newModel.setNsPrefix("", baseURI);
		return newModel;
	}

	public static Model makeReadOnly(Model model) {
		return model;
	}

	public static void main(String[] args) {
		int argslength = args.length;
		if (argslength > 0) {
			if (args[0].equals("--uri"))
			{
				if (argslength < 2) {
					System.out.println("Not enough arguments.  "
							+ "Expected: --uri [URI] [Output Directory] "
							+ "(ex. --uri goog:/0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c/9/8 GluePuma_R25_TestFull)");
					return;
				}
				List<ClassLoader> fileModelCLs = Arrays.asList(ClassLoader.getSystemClassLoader());
				String dirModelURL = args[1];
				Repo repo;
				try {
					RepoSpec repospec = new URLRepoSpec(dirModelURL, fileModelCLs);
					repo = repospec.makeRepo();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("Bad URI: " + args[1]);
					return;
				}

				String dir = "OutputDir";
				if (argslength > 2) {
					dir = args[2];
				}
				writeRepo(repo, dir);
				return;
			}
		}
		if (args.length < 4) {
			System.out.println("Not enough arguments.  "
					+ "Expected: [SheetKey] [Namespace Tab] [Dir Tab] [Output Directory] "
					+ "(ex. 0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c 9 8 GluePuma_R25_TestFull)");
			return;
		}
		String key = args[0].trim();
		String out = args[3].trim();
		Integer nmspc;
		Integer dir;
		try {
			nmspc = Integer.parseInt(args[1].trim());
			dir = Integer.parseInt(args[2].trim());
		} catch (NumberFormatException ex) {
			System.out.println("Bad Namespace or Dir number: " + args[1] + ", " + args[2]);
			return;
		}
		List<ClassLoader> loaders = Arrays.asList(ClassLoader.getSystemClassLoader());
		OnlineSheetRepoSpec repo = new OnlineSheetRepoSpec(key, nmspc, dir, loaders);
		writeRepo(repo.makeRepo(), out);
	}

	private static void writeRepo(Repo repo, String dir) {
		if (!(repo instanceof Repo.WithDirectory)) {
			System.out.println("Not Repo.WithDirectory  " + repo.getClass() + " " + repo);
			return;
		}
		try {
			File d = new File(dir);
			System.out.println("Writing repo to " + d.getAbsolutePath() + " ...");
			d.delete();
			RepoOper.writeRepoToDirectory(repo, dir);
			System.out.println("Writing repo complete");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
