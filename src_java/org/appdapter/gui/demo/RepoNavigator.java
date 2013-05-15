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
package org.appdapter.gui.demo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.Repo.WithDirectory;
import org.appdapter.demo.DemoServiceWrapFuncs;
import org.appdapter.demo.ObjectNavigatorGUI;
import org.appdapter.gui.box.DisplayContextProvider;
import org.appdapter.gui.box.ScreenBoxContextImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.demo.triggers.BridgeTriggers;
import org.appdapter.gui.demo.triggers.DatabaseTriggers;
import org.appdapter.gui.demo.triggers.RepoTriggers;
import org.appdapter.gui.pojo.POJOCollectionWithSwizzler;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.repo.MutableRepoBox;
import org.appdapter.gui.repo.RepoBox;
import org.appdapter.gui.repo.RepoBoxImpl;
import org.appdapter.gui.repo.RepoModelBoxImpl;
import org.appdapter.gui.trigger.BootstrapTriggerFactory;
import org.appdapter.gui.trigger.SysTriggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;

//import org.cogchar.blob.emit.OmniLoaderRepo;
/**
 * @author Stu B. <www.texpedient.com>
 */
// / Dmiles needed something in java to cover Scala blindspots
public class RepoNavigator extends DemoNavigatorCtrl implements ObjectNavigatorGUI {

	private TreeModel myTM;
	private BoxContext myBoxCtx;
	private ScreenBoxTreeNode myRootBTN;
	private DisplayContextProvider myDCP;
	private JComponent desk;

	@Override public void showMessage(String string) {
		getBrowsePanel().getBoxPanelStatus().setText(string);
	}

	public RepoNavigator(BoxContext bc, TreeModel tm, ScreenBoxTreeNode rootBTN, DisplayContextProvider dcp) {
		super(bc, tm, rootBTN, dcp);
		Utility.repoNav = this;
		myBoxCtx = bc;
		myTM = tm;
		myRootBTN = rootBTN;
		myDCP = dcp;
		desk = new JDesktopPane();
	}

	public void addRepo(String labeled, Repo.WithDirectory inner) {
		// (MutableScreenBoxForImmutableRepo)makeRepoChildBoxImpl(rootBox,
		// MutableScreenBoxForImmutableRepo.class,
		// ReloadTrigger.class,
		// labeled,
		// finderRepo);
		BootstrapTriggerFactory btf = new BootstrapTriggerFactory();

		Class repoBoxClass = MutableScreenBoxForImmutableRepo.class;
		RepoTriggers.DumpStatsTrigger dumpStatsTrigger = new RepoTriggers.DumpStatsTrigger();
		MutableScreenBoxForImmutableRepo r1Box = (MutableScreenBoxForImmutableRepo) makeRepoBoxImpl(repoBoxClass, dumpStatsTrigger, labeled, inner);
		if (inner instanceof Repo.Stored) {
			btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
			btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
		}
		btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
		btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
		btf.attachTrigger(r1Box, new RepoOper.ReloadAllModelsTrigger(inner), "reload repo");
		btf.attachTrigger(r1Box, dumpStatsTrigger, "dump stats");
		btf.attachTrigger(r1Box, new SysTriggers.DumpTrigger(), "dump");
		btf.attachTrigger(r1Box, new BridgeTriggers.MountSubmenuFromTriplesTrigger(), "loadSubmenus");
		DemoServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", ScreenBoxPanel.Kind.REPO_MANAGER);
		addBoxToRoot(r1Box, true);
	}

	@Override public void addBoxToRoot(MutableBox childBox, boolean reload) {

		// Add the child
		Box rootBox = myBoxCtx.getRootBox();
		myBoxCtx.contextualizeAndAttachChildBox(rootBox, childBox);
		if (reload) {
			((DefaultTreeModel) myTM).reload();
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(RepoNavigator.class);

	public static void testLoggingSetup() {
		System.out.println("[System.out] - RepoNavigator.pretendToBeAwesome()");
		theLogger.info("[SLF4J] - RepoNavigator.pretendToBeAwesome()");
	}

	public static void main(String[] args) throws InterruptedException {
		testLoggingSetup();
		theLogger.info("RepoNavigator.main()-START");
		RepoNavigator dnc = makeRepoNavigatorCtrl(args);
		// .main(args);
		dnc.launchFrame("Appdapter Repo Browser");
		theLogger.info("RepoNavigator.main()-END");
		Thread.sleep(100000);
	}

	public static interface RepoSubBoxFinder {

		public Box findGraphBox(RepoBox parentBox, String graphURI);
	}

	public static RepoSubBoxFinder theRSBF;

	public static class RepoRepoBoxImpl<TT extends Trigger<? extends RepoBoxImpl<TT>>> extends RepoBoxImpl<TT> {

		RepoSubBoxFinder myRSBF;

		@Override public Box findGraphBox(String graphURI) {
			if (myRSBF == null) {
				myRSBF = theRSBF;
			}
			return myRSBF.findGraphBox(this, graphURI);
		}
	}

	public static RepoNavigator makeRepoNavigatorCtrl(String[] args) {
		RepoSubBoxFinder rsbf = new RepoSubBoxFinder() {

			@Override public Box findGraphBox(RepoBox parentBox, String graphURI) {

				theLogger.info("finding graph box for " + graphURI + " in " + parentBox);
				MutableBox mb = new RepoModelBoxImpl();
				TriggerImpl dti = new SysTriggers.DumpTrigger();
				dti.setShortLabel("DumpTrigger " + graphURI);
				mb.attachTrigger(dti);

				Repo parentRepo = parentBox.getRepo();
				if (parentRepo instanceof RepoOper.Reloadable) {
					dti = new RepoOper.ReloadSingleModelTrigger(graphURI, (RepoOper.Reloadable) parentRepo);
					dti.setShortLabel("reload model " + graphURI);
					mb.attachTrigger(dti);
				}

				return mb;
			}
		};
		RepoNavigator dnc = makeRepoNavigatorCtrl(args, rsbf);
		return dnc;
	}

	public static RepoNavigator makeRepoNavigatorCtrl(String[] args, RepoSubBoxFinder rsbf) {
		theRSBF = rsbf;
		// From this BoxImpl.class, is makeBCI is able to infer the full
		// BT=BoxImpl<... tree?
		return makeRepoNavigatorCtrl(args, ScreenBoxImpl.class, RepoRepoBoxImpl.class);
	}

	public static RepoNavigator makeRepoNavigatorCtrl(String[] args, Class<? extends ScreenBoxImpl> boxClass, Class<? extends RepoBoxImpl> repoBoxClass) {
		// From this BoxImpl.class, is makeBCI is able to infer the full
		// BT=BoxImpl<... tree?
		ScreenBoxContextImpl bctx = makeBCI(boxClass, repoBoxClass);
		TreeModel tm = bctx.getTreeModel();
		ScreenBoxTreeNode rootBTN = (ScreenBoxTreeNode) tm.getRoot();

		DisplayContextProvider dcp = bctx;
		RepoNavigator tn = new RepoNavigator(bctx, tm, rootBTN, dcp);
		return tn;
	}

	public static <BT extends ScreenBoxImpl<?>, RBT extends RepoBoxImpl<?>> ScreenBoxContextImpl makeBCI(Class<BT> boxClass, Class<RBT> repoBoxClass) {
		TriggerImpl regTrigProto = makeTriggerPrototype(boxClass);
		TriggerImpl repoTrigProto = makeTriggerPrototype(repoBoxClass);
		return makeBoxContextImpl(boxClass, repoBoxClass, regTrigProto, repoTrigProto);
	}

	public static <BT extends ScreenBoxImpl<?>> TriggerImpl<?> makeTriggerPrototype(Class<BT> boxClass) {
		// The trigger subtype does not matter - what matters is capturing BT
		// into the type.
		return new SysTriggers.QuitTrigger();
	}

	static class ScreenModelBox extends ScreenBoxImpl {

		final String myURI;
		private Model myModel;

		public ScreenModelBox(String uri) {
			myURI = uri;
		}

		@Override public String toString() {
			return getClass().getName() + "[uri=" + myURI + "model=" + myModel + "]";
		}

		// setShortLabel("tweak-" + myURI);
		public void setModel(Model m) {
			this.myModel = m;
		}
	}

	static class ScreenGraphTrigger extends TriggerImpl /*
													   * with FullTrigger<GraphBox>
													   */{

		final String myDebugName;

		public ScreenGraphTrigger(String myDebugNym) {
			myDebugName = myDebugNym;
		}

		@Override public String toString() {
			return getClass().getName() + "[name=" + myDebugName + "]";
		}

		@Override public void fire(Box targetBox) {
			getLogger().debug(this.toString() + " firing on " + targetBox.toString());

		}
	}

	static class MutableScreenBoxForImmutableRepo<TT extends Trigger<? extends RepoBoxImpl<TT>>> extends RepoRepoBoxImpl<TT> implements MutableRepoBox<TT> {

		final Repo.WithDirectory myRepoWD;
		final String myDebugName;
		public List<MutableRepoBox> childBoxes = new ArrayList<MutableRepoBox>();

		public MutableScreenBoxForImmutableRepo(String myDebugNym, Repo.WithDirectory repo) {
			myDebugName = myDebugNym;
			myRepoWD = (WithDirectory) repo;
			// resyncChildrenToTree();
		}

		void resyncChildrenToTree() {
			BoxContext ctx = getBoxContext();
			List<Repo.GraphStat> graphStats = getAllGraphStats();
			Repo.WithDirectory repo = getRepoWD();

			// OmniLoaderRepo fr = (OmniLoaderRepo) repo;//
			// repo.getDirectoryModelClient();
			QuerySolution qInitBinding = null;
			String qText = "";
			/*ResultSet rset = QueryHelper.execModelQueryWithPrefixHelp(repo.getDirectoryModel(), "select distinct ?s ?o {?s a ?o}");

			// cp to list (since will be doing this differntly later)
			List<QuerySolution> solnList = new ArrayList<QuerySolution>();
			while (rset.hasNext()) {
				QuerySolution qsoln = rset.next();
				solnList.add(qsoln);
			}

			for (QuerySolution gs : solnList) {
				String constituentRepoName = gs.getResource("s").asNode().getURI();
				ScreenModelBox graphBox = new ScreenModelBox(constituentRepoName);
				ScreenGraphTrigger gt = new ScreenGraphTrigger(constituentRepoName);
				gt.setShortLabel("have-some-fun with Repo " + constituentRepoName + " type " + gs.get("o"));
				graphBox.attachTrigger(gt);
				ctx.contextualizeAndAttachChildBox(this, graphBox);
			}

			for (Repo.GraphStat gs : graphStats) {
				ScreenModelBox graphBox = new ScreenModelBox(gs.graphURI);
				ScreenGraphTrigger gt = new ScreenGraphTrigger("graph=" + gs.graphURI);
				gt.setShortLabel("have-some-fun with uri=" + gs);
				graphBox.attachTrigger(gt);
				ctx.contextualizeAndAttachChildBox(this, graphBox);
			}*/
		}

		@Override public Repo getRepo() {
			// TODO Auto-generated method stub
			return myRepoWD;
		}

		public Repo.WithDirectory getRepoWD() {
			// TODO Auto-generated method stub
			return myRepoWD;
		}

		@Override public List getAllGraphStats() {
			Repo myRepo = getRepo();
			return myRepo.getGraphStats();
		}

		@Override public Box findGraphBox(String graphURI) {
			Logger logger = theLogger;

			Box fnd = super.findGraphBox(graphURI);
			boolean madeAlready = false;
			if (fnd != null) {
				logger.trace("Found graphURI=" + graphURI + " on super.findGraphBox" + fnd);
				madeAlready = true;
			}

			BoxContext ctx = getBoxContext();
			List<Repo.GraphStat> graphStats = getAllGraphStats();
			Model m = myRepoWD.getNamedModel(new FreeIdent(graphURI));

			for (Repo.GraphStat gs : graphStats) {
				if (gs.graphURI.equals(graphURI)) {
					ScreenModelBox graphBox = new ScreenModelBox(gs.graphURI);
					graphBox.setModel(m);
					ScreenGraphTrigger gt = new ScreenGraphTrigger(gs.graphURI);
					graphBox.attachTrigger(gt);
					if (!madeAlready) {
						ctx.contextualizeAndAttachChildBox(this, graphBox);
					}
					return (Box) graphBox;
				}
			}

			fnd = super.findGraphBox(graphURI);

			if (fnd != null) {
				logger.trace("Wierdly!?! Found graphURI=" + graphURI + " on super.findGraphBox " + fnd);
				return fnd;
			}

			logger.trace("NOT FOUND graphURI=" + graphURI + " on findGraphBox");
			return null;
		}

		@Override public void mount(String configPath) {
			super.mount(configPath);
		}

		@Override public void formatStoreIfNeeded() {
			super.formatStoreIfNeeded();
		}

		@Override public void importGraphFromURL(String tgtGraphName, String sourceURL, boolean replaceTgtFlag) {
			super.importGraphFromURL(tgtGraphName, sourceURL, replaceTgtFlag);
		}

		@Override public String getUploadHomePath() {
			return super.getUploadHomePath();
		}
	}

	/**
	 * Here is a humdinger of a static method, that constructs a demontration application tree
	 *
	 * @param <BT>
	 * @param <RBT>
	 * @param regBoxClass
	 * @param repoBoxClass
	 * @param regTrigProto - defines the BT trigger parameter type for screen boxes. The regTrigProto instance data is
	 * unused.
	 * @param repoTrigProto - defines the RBT trigger parameter type for repo boxes. The repoTrigProto instance data is
	 * unused.
	 * @return
	 */
	static public <BT extends ScreenBoxImpl, RBT extends RepoBoxImpl> ScreenBoxContextImpl makeBoxContextImpl(Class<BT> regBoxClass, Class<RBT> repoBoxClass, TriggerImpl regTrigProto,
			TriggerImpl repoTrigProto) {
		try {
			ScreenBoxContextImpl bctx = new ScreenBoxContextImpl();

			BT rootBox = (BT) DemoServiceWrapFuncs.makeTestBoxImpl((Class) regBoxClass, regTrigProto, "rooty");
			bctx.contextualizeAndAttachRootBox(rootBox);

			BootstrapTriggerFactory btf = new BootstrapTriggerFactory();
			btf.attachTrigger(rootBox, (TriggerImpl) new SysTriggers.QuitTrigger(), "quit");

			BT repoBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, regTrigProto, "repo");
			BT appBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, regTrigProto, "app");
			BT sysBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, regTrigProto, "sys");

			if (false) {

				/**
				 * Good examples of making boxes
				 */
				{
					RBT r1Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, repoBoxClass, repoTrigProto, "h2.td_001");

					btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
					btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
					btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
					btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
					btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
					btf.attachTrigger(r1Box, new RepoTriggers.DumpStatsTrigger(), "dump stats");
					DemoServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", ScreenBoxPanel.Kind.REPO_MANAGER);

					r1Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, repoBoxClass, repoTrigProto, "h2.td_002");

					btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
					btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
					btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
					btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
					btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
					btf.attachTrigger(r1Box, new RepoTriggers.DumpStatsTrigger(), "dump stats");
					DemoServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", ScreenBoxPanel.Kind.REPO_MANAGER);
				}

				RBT r2Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, repoBoxClass, repoTrigProto, "repo_002");
				btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpD");
				btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpC");
				btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpA");

				BT fishBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "fishy");
				DemoServiceWrapFuncs.attachPanelOpenTrigger(fishBox, "open-matrix-f", ScreenBoxPanel.Kind.MATRIX);

				btf.attachTrigger(fishBox, new SysTriggers.DumpTrigger(), "dumpF");

				BT pumappBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "pumapp");
				DemoServiceWrapFuncs.attachPanelOpenTrigger(pumappBox, "open-matrix-p", ScreenBoxPanel.Kind.MATRIX);
				btf.attachTrigger(pumappBox, new SysTriggers.DumpTrigger(), "dumpP");

				BT buckTreeBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "bucksum");
				btf.attachTrigger(buckTreeBox, new BridgeTriggers.MountSubmenuFromTriplesTrigger(), "loadSubmenus");

				/*
				 * makeChildNode(appNode, "custy"); makeChildNode(appNode, "rakedown");
				 *
				 * makeChildNode(sysNode, "memory"); makeChildNode(sysNode, "log"); makeChildNode(sysNode, "job");
				 */
			}

			return bctx;
		} catch (Throwable t) {
			theLogger.error("problem in tree init", t);
			return null;
		}

	}

	public <BT extends ScreenBoxImpl> BT makeRepoChildBoxImpl(Box parentBox, Class<BT> childBoxClass, TriggerImpl trigProto, String label, Repo.WithDirectory inner) {
		BT result = null;
		BoxContext ctx = parentBox.getBoxContext();
		result = makeRepoBoxImpl(childBoxClass, trigProto, label, inner);
		ctx.contextualizeAndAttachChildBox(parentBox, result);
		return result;
	}

	public <BT extends ScreenBoxImpl> BT makeRepoBoxImpl(Class<BT> boxClass, TriggerImpl trigProto, String label, Repo.WithDirectory inner) {
		MutableScreenBoxForImmutableRepo result = new MutableScreenBoxForImmutableRepo(label, inner);// CachingComponentAssembler.makeEmptyComponent(boxClass);
		result.setShortLabel(label);
		// set the child's BoxContext (redundant since the next line does it)
		BoxContext cctx = result.getBoxContext();
		if (cctx == null) {
			result.setContext(myBoxCtx);
		}

		result.setDescription("full description for " + boxClass.getName() + " with label: " + label);
		return (BT) (Object) result;
	}

	public JTabbedPane getBoxPanelTabPane() {
		// TODO Auto-generated method stub
		return getBrowsePanel().getBoxPanelTabPane();
	}

	public JPanel getBoxPanel() {
		// TODO Auto-generated method stub
		return getBrowsePanel();
	}

	@Override public POJOCollectionWithSwizzler getCollectionWithSwizzler() {
		return Utility.context;
	}

	@Override public JComponent getDesk() {
		return desk;
	}
}
