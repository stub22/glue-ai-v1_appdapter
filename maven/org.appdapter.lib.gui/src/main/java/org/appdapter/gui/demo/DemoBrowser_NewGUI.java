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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayContextProvider;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.ScreenBox.Kind;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.Repo.WithDirectory;
import org.appdapter.core.store.RepoBox;
import org.appdapter.demo.DemoBrowserCtrl;
import org.appdapter.demo.DemoBrowserUI;
import org.appdapter.demo.DemoNavigatorCtrlFactory;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.box.ScreenBoxContextImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.browse.ScreenBoxTreeNodeImpl;
import org.appdapter.gui.demo.triggers.BridgeTriggers;
import org.appdapter.gui.demo.triggers.DatabaseTriggers;
import org.appdapter.gui.demo.triggers.RepoTriggers;
import org.appdapter.gui.rimpl.BootstrapTriggerFactory;
import org.appdapter.gui.rimpl.MutableRepoBox;
import org.appdapter.gui.rimpl.RepoBoxImpl;
import org.appdapter.gui.rimpl.RepoModelBoxImpl;
import org.appdapter.gui.rimpl.SysTriggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Stu B. <www.texpedient.com>
 */
final public class DemoBrowser_NewGUI {
	static Logger theLogger = getLogger();

	public static Logger getLogger() {
		try {
			theLogger = LoggerFactory.getLogger(DemoBrowser_NewGUI.class);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return theLogger;
	}

	public static void testLoggingSetup() {
		System.out.println("[System.out] - DemoBrowser.pretendToBeAwesome()");
		theLogger.info("[SLF4J] - DemoBrowser.pretendToBeAwesome()");
	}

	static public boolean defaultExampleCode = false;

	// ==== Main method ==========================
	public static void main(String[] args) throws InterruptedException {
		testLoggingSetup();
		theLogger.info("DemoBrowser.main()-START");
		DemoBrowserCtrl dnc;
		try {
			//ObjectNavigator frame = new ObjectNavigator();
			//Utility.setInstancesOfObjects(frame.getChildCollectionWithContext());
			// frame.pack();
			DemoNavigatorCtrlFactory crtlMaker = new DemoNavigatorCtrlFactory() {

				@Override public DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] main, boolean defaultExampleCode) {
					return DemoBrowser_NewGUI.makeDemoNavigatorCtrl(main, defaultExampleCode);
				}

			};
			DemoBrowserUI.registerDemo(crtlMaker);
			//frame.setSize(800, 600);
			//org.appdapter.gui.pojo.Utility.centerWindow(frame);
			dnc = DemoBrowserUI.makeDemoNavigatorCtrl(args);
			dnc.launchFrame("This is ObjectNavigator");
			//frame.show();
			//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			theLogger.info("ObjectNavigator is now running!");
		} catch (Exception err) {
			theLogger.error("ObjectNavigator could not be started", err);
		}
		theLogger.info("DemoBrowser.main()-END");
	}

	static public class AsApplet extends JApplet {
		@Override public void init() {
			javax.swing.Box box = new javax.swing.Box(BoxLayout.Y_AXIS);
			try {
				getContentPane().setLayout(new BorderLayout());
				getContentPane().add("Center", box);
				box.add(new JLabel("Opening DemoNavigatorCtrl in a new window..."));
				DemoNavigatorCtrl dnc = makeDemoNavigatorCtrl(new String[0], false);
				dnc.launchFrame("Appdapter Demo Browser");
				setVisible(false);
				setSize(0, 0);
			} catch (Exception err) {
				JTextArea text = new JTextArea();
				text.setEditable(false);
				text.setText("Darn, an error occurred!\nPlease email this to henrik@kniberg.com, thanks!\n\n" + err.toString());
				box.add(text);
			}
		}
	}

	public static interface RepoSubBoxFinder {
		public Box findGraphBox(RepoBox parentBox, String graphURI);
	}

	public static RepoSubBoxFinder theRSBF;

	public static class DemoRepoBoxImpl_NewGUI extends RepoBoxImpl {
		RepoSubBoxFinder myRSBF;

		@Override public Box findGraphBox(String graphURI) {
			if (myRSBF == null) {
				myRSBF = theRSBF;
			}
			return myRSBF.findGraphBox(this, graphURI);
		}
	}

	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args, boolean isExampleCode) {
		RepoSubBoxFinder rsbf = new RepoSubBoxFinder() {
			@Override public Box findGraphBox(RepoBox parentBox, String graphURI) {
				theLogger.info("finding graph box for " + graphURI + " in " + parentBox);
				MutableBox mb = new RepoModelBoxImpl();
				TriggerImpl dti = new SysTriggers.DumpTrigger();
				dti.setShortLabel("ping-" + graphURI);
				mb.attachTrigger(dti);

				Repo parentRepo = parentBox.getRepo();

				return mb;
			}
		};
		DemoNavigatorCtrl dnc = (DemoNavigatorCtrl) makeDemoNavigatorCtrl(args, rsbf, isExampleCode);
		return dnc;
	}

	public static BaseDemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args, RepoSubBoxFinder rsbf, boolean isExampleCode) {
		theRSBF = rsbf;
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		return makeDemoNavigatorCtrl(args, ScreenBoxImpl.class, DemoRepoBoxImpl_NewGUI.class, isExampleCode);
	}

	public static BaseDemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args, Class<? extends ScreenBoxImpl> boxClass, Class<? extends RepoBoxImpl> repoBoxClass, boolean isExampleCode) {
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		ScreenBoxContextImpl bctx = makeBCI(boxClass, repoBoxClass, isExampleCode);
		TreeModel tm = bctx.getTreeModel();
		ScreenBoxTreeNodeImpl rootBTN = (ScreenBoxTreeNodeImpl) tm.getRoot();

		DisplayContextProvider dcp = bctx;

		BaseDemoNavigatorCtrl tn = new RepoNavigator(bctx, tm, (ScreenBoxTreeNodeImpl) rootBTN, dcp);
		return tn;
	}

	/**
	 * <code>
	 * 	<BT extends ScreenBoxImpl<TriggerImpl<BT>>, RBT extends RepoBoxImpl<TriggerImpl<RBT>>> 
	 *		ScreenBoxContextImpl makeBCI(Class<BT> boxClass, Class<RBT> repoBoxClass) {.. }
	 * </code>
	 */
	public static <BTI extends TriggerImpl<BT>, BT extends ScreenBoxImpl<BTI>, RBTI extends TriggerImpl<RBT>, RBT extends RepoBoxImpl<RBTI>> ScreenBoxContextImpl makeBCI(Class<BT> boxClass,
			Class<RBT> repoBoxClass, boolean isExampleCode) {
		TriggerImpl<BT> regTrigProto = makeTriggerPrototype(boxClass);
		TriggerImpl<RBT> repoTrigProto = makeTriggerPrototype(repoBoxClass);
		return makeBoxContextImpl(boxClass, repoBoxClass, regTrigProto, repoTrigProto, isExampleCode);
	}

	@SuppressWarnings("unchecked") public static <BTS extends TriggerImpl<BT>, BT extends ScreenBoxImpl<BTS>, TBT extends TriggerImpl<BT>> TBT makeTriggerPrototype(Class<BT> boxClass) {
		// The trigger subtype does not matter - what matters is capturing BT into the type.
		return (TBT) new SysTriggers.QuitTrigger().makeTrigger(boxClass);
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

	public static class RepoRepoBoxImpl<TT extends Trigger<? extends RepoBoxImpl<TT>>> extends RepoBoxImpl<TT> {

		RepoSubBoxFinder myRSBF;

		@Override public Box findGraphBox(String graphURI) {
			if (myRSBF == null) {
				myRSBF = theRSBF;
			}
			return myRSBF.findGraphBox(this, graphURI);
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
			return myRepoWD;
		}

		public Repo.WithDirectory getRepoWD() {
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

	// static class ConcBootstrapTF extends BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
	// }  //   TT extends TriggerImpl<BT>

	/** Here is a humdinger of a static method, that constructs a demontration application tree
	 * 
	 * @param <BT>
	 * @param <RBT>
	 * @param regBoxClass
	 * @param repoBoxClass
	 * @param regTrigProto - defines the BT  trigger parameter type for screen boxes.  The regTrigProto instance data is unused.
	 * @param repoTrigProto - defines the RBT trigger parameter type for repo boxes.  The repoTrigProto instance data is unused.
	 * @return 
	 */
	public static <TBT extends TriggerImpl<BT>, BT extends ScreenBoxImpl<TBT>, TRBT extends TriggerImpl<RBT>, RBT extends RepoBoxImpl<TRBT>> ScreenBoxContextImpl makeBoxContextImpl(
			Class<BT> regBoxClass, Class<RBT> repoBoxClass, TriggerImpl<BT> regTrigProto, TriggerImpl<RBT> repoTrigProto, boolean isExampleCode) {
		try {
			ScreenBoxContextImpl bctx = new ScreenBoxContextImpl();

			BT rootBox = (BT) DemoServiceWrapFuncs.makeTestBoxImpl((Class) regBoxClass, (TriggerImpl) regTrigProto, "All Objects");
			bctx.contextualizeAndAttachRootBox(rootBox);

			BootstrapTriggerFactory btf = new BootstrapTriggerFactory();
			btf.attachTrigger(rootBox, new SysTriggers.QuitTrigger(), "quit");

			TriggerImpl regTrigProtoE = regTrigProto;

			BT repoBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, regTrigProtoE, "repo");
			BT appBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, regTrigProtoE, "app");
			BT sysBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, regTrigProtoE, "sys");

			//if (!isExampleCode) 				return bctx;

			RBT r1Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, (Class) repoBoxClass, regTrigProtoE, "h2.td_001");

			btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
			btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
			btf.attachTrigger(r1Box, new RepoTriggers.DumpStatsTrigger(), "dump stats");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", Kind.REPO_MANAGER);

			RBT r2Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, (Class) repoBoxClass, regTrigProtoE, "repo_002");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpD");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpC");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpA");

			BT fishBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, regTrigProtoE, "fishy");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(fishBox, "open-matrix-f", Kind.MATRIX);

			btf.attachTrigger(fishBox, new SysTriggers.DumpTrigger(), "dumpF");

			BT pumappBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, regTrigProtoE, "pumapp");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(pumappBox, "open-matrix-p", Kind.MATRIX);
			btf.attachTrigger(pumappBox, new SysTriggers.DumpTrigger(), "dumpP");

			BT buckTreeBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, regTrigProtoE, "bucksum");
			btf.attachTrigger(buckTreeBox, new BridgeTriggers.MountSubmenuFromTriplesTrigger(), "loadSubmenus");

			/*
						makeChildNode(appNode, "custy");
						makeChildNode(appNode, "rakedown");

						makeChildNode(sysNode, "log");
						makeChildNode(sysNode, "job");
						*/
			return bctx;
		} catch (Throwable t) {
			theLogger.error("problem in tree init", t);
			return null;
		}

	}

	/*
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
	*/
}
