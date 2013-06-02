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

import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.store.Repo;
import org.appdapter.demo.DemoServiceWrapFuncs;
import org.appdapter.gui.box.DisplayContextProvider;
import org.appdapter.gui.box.ScreenBoxContextImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.demo.triggers.BridgeTriggers;
import org.appdapter.gui.demo.triggers.DatabaseTriggers;
import org.appdapter.gui.demo.triggers.RepoTriggers;
import org.appdapter.gui.repo.RepoBox;
import org.appdapter.gui.repo.RepoBoxImpl;
import org.appdapter.gui.repo.RepoModelBoxImpl;
import org.appdapter.gui.trigger.BootstrapTriggerFactory;
import org.appdapter.gui.trigger.SysTriggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoBrowser {
	static Logger theLogger = LoggerFactory.getLogger(DemoBrowser.class);

	public static void testLoggingSetup() {
		System.out.println("[System.out] - DemoBrowser.pretendToBeAwesome()");
		theLogger.info("[SLF4J] - DemoBrowser.pretendToBeAwesome()");
	}
	public static void main(String[] args) {
		testLoggingSetup();
		theLogger.info("DemoBrowser.main()-START");
		DemoNavigatorCtrl dnc = makeDemoNavigatorCtrl(args);
		dnc.launchFrame("Appdapter Demo Browser");
		theLogger.info("DemoBrowser.main()-END");
	}

	public static interface RepoSubBoxFinder {
		public Box findGraphBox(RepoBox parentBox, String graphURI);
	}
	public static RepoSubBoxFinder theRSBF;
	public static class DemoRepoBoxImpl extends RepoBoxImpl {
		RepoSubBoxFinder myRSBF;
		
		@Override public Box findGraphBox(String graphURI) {
			if (myRSBF == null) {
				myRSBF = theRSBF;
			}
			return myRSBF.findGraphBox(this, graphURI);
		}
	}

	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args) {
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
		DemoNavigatorCtrl dnc = makeDemoNavigatorCtrl(args, rsbf);
		return dnc;
	}
	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args, RepoSubBoxFinder rsbf) {
		theRSBF = rsbf;
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		return makeDemoNavigatorCtrl(args, ScreenBoxImpl.class, DemoRepoBoxImpl.class);
	}

	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args, Class<? extends ScreenBoxImpl> boxClass, Class<? extends RepoBoxImpl> repoBoxClass) {
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		ScreenBoxContextImpl bctx = makeBCI(boxClass, repoBoxClass);
		TreeModel tm = bctx.getTreeModel();
		ScreenBoxTreeNode rootBTN = (ScreenBoxTreeNode) tm.getRoot();

		DisplayContextProvider dcp = bctx;
		DemoNavigatorCtrl tn = new DemoNavigatorCtrl(bctx, tm, rootBTN, dcp);
		return tn;
	}

	public static <BT extends ScreenBoxImpl<BTI>, BTI extends TriggerImpl<BT>, RBT extends RepoBoxImpl<RBTI>, RBTI extends TriggerImpl<RBT>> ScreenBoxContextImpl makeBCI(Class<BT> boxClass,
			Class<RBT> repoBoxClass) {
		TriggerImpl<BT> regTrigProto = makeTriggerPrototype(boxClass);
		TriggerImpl<RBT> repoTrigProto = makeTriggerPrototype(repoBoxClass);
		return makeBoxContextImpl(boxClass, repoBoxClass, regTrigProto, repoTrigProto);
	}

	public static <BT extends ScreenBoxImpl<BTI>, BTI extends TriggerImpl<BT>> TriggerImpl<BT> makeTriggerPrototype(Class<BT> boxClass) {
		// The trigger subtype does not matter - what matters is capturing BT into the type.
		return (BTI) new SysTriggers.QuitTrigger();
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
	public static <BT extends ScreenBoxImpl<BTS>, BTS extends TriggerImpl<BT>, RBT extends RepoBoxImpl<RBTT>, RBTT extends TriggerImpl<RBT>> ScreenBoxContextImpl makeBoxContextImpl(
			Class<BT> regBoxClass, Class<RBT> repoBoxClass, TriggerImpl<BT> regTrigProto, TriggerImpl<RBT> repoTrigProto) {
		try {
			ScreenBoxContextImpl bctx = new ScreenBoxContextImpl();
			BT rootBox = (BT) DemoServiceWrapFuncs.makeTestBoxImpl((Class) regBoxClass, (BTS) regTrigProto, "rooty");
			bctx.contextualizeAndAttachRootBox(rootBox);

			BootstrapTriggerFactory btf = new BootstrapTriggerFactory();
			btf.attachTrigger(rootBox, new SysTriggers.QuitTrigger(), "quit");

			BT repoBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, (BTS) regTrigProto, "repo");
			BT appBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, (BTS) regTrigProto, "app");
			BT sysBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, (Class) regBoxClass, (BTS) regTrigProto, "sys");

			RBT r1Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, (Class) repoBoxClass, (BTS) repoTrigProto, "h2.td_001");

			btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
			btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
			btf.attachTrigger(r1Box, new RepoTriggers.DumpStatsTrigger(), "dump stats");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", ScreenBoxPanel.Kind.REPO_MANAGER);

			RBT r2Box = (RBT) DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, (Class) repoBoxClass, (BTS) repoTrigProto, "repo_002");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpD");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpC");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpA");

			BT fishBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, (BTS) regTrigProto, "fishy");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(fishBox, "open-matrix-f", ScreenBoxPanel.Kind.MATRIX);

			btf.attachTrigger(fishBox, new SysTriggers.DumpTrigger(), "dumpF");

			BT pumappBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, (BTS) regTrigProto, "pumapp");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(pumappBox, "open-matrix-p", ScreenBoxPanel.Kind.MATRIX);
			btf.attachTrigger(pumappBox, new SysTriggers.DumpTrigger(), "dumpP");

			BT buckTreeBox = (BT) DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, (Class) regBoxClass, (BTS) regTrigProto, "bucksum");
			btf.attachTrigger(buckTreeBox, new BridgeTriggers.MountSubmenuFromTriplesTrigger(), "loadSubmenus");
/*
			makeChildNode(sysNode, "log");
			makeChildNode(sysNode, "job");
						makeChildNode(appNode, "rakedown");

						makeChildNode(sysNode, "log");
						makeChildNode(sysNode, "job");
									makeChildNode(appNode, "rakedown");

									makeChildNode(sysNode, "memory");
									makeChildNode(sysNode, "log");
									makeChildNode(sysNode, "job");
						*/
			return bctx;
		} catch (Throwable t) {
			theLogger.error("problem in tree init", t);
			return null;
		}

	}
}
