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

import org.appdapter.demo.DemoServiceWrapFuncs;
import javax.swing.tree.TreeModel;
import org.appdapter.gui.box.BoxContextImpl;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.BoxPanel;
import org.appdapter.gui.box.BoxTreeNode;
import org.appdapter.gui.box.DisplayContextProvider;
import org.appdapter.gui.box.TriggerImpl;
import org.appdapter.gui.repo.RepoBoxImpl;
import org.appdapter.gui.trigger.BootstrapTriggerFactory;
import org.appdapter.gui.demo.triggers.BridgeTriggers;
import org.appdapter.gui.demo.triggers.DatabaseTriggers;
import org.appdapter.gui.demo.triggers.RepoTriggers;
import org.appdapter.gui.trigger.SysTriggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoBrowser {
	static Logger theLogger = LoggerFactory.getLogger(DemoBrowser.class);

	public static void pretendToBeAwesome() {
		System.out.println("[System.out] - DemoBrowser.pretendToBeAwesome()");
		theLogger.info("[SLF4J] - DemoBrowser.pretendToBeAwesome()");
	}
	public static void main(String[] args) {
		pretendToBeAwesome();
		theLogger.info("DemoBrowser.main()-START");
		DemoNavigatorCtrl tn = makeDemoNavigatorCtrl(args);
		tn.launchFrame("Appdapter Demo Browser");
		theLogger.info("DemoBrowser.main()-END");		
	}
	public static DemoNavigatorCtrl makeDemoNavigatorCtrl(String[] args) {
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		BoxContextImpl bctx = makeBCI(BoxImpl.class, RepoBoxImpl.class);// makeBoxContextImpl(BoxImpl.class, TriggerImpl.class);
		TreeModel tm = bctx.getTreeModel();
		BoxTreeNode rootBTN = (BoxTreeNode) tm.getRoot();

		DisplayContextProvider dcp = bctx;
		DemoNavigatorCtrl tn = new DemoNavigatorCtrl(bctx, tm, rootBTN, dcp);
		return tn;
	}
	public static <BT extends BoxImpl<TriggerImpl<BT>>, RBT extends RepoBoxImpl<TriggerImpl<RBT>>> BoxContextImpl makeBCI(Class<BT> boxClass, Class<RBT> repoBoxClass) {
		TriggerImpl<BT> regTrigProto = makeTriggerPrototype(boxClass);
		TriggerImpl<RBT> repoTrigProto = makeTriggerPrototype(repoBoxClass);
		return makeBoxContextImpl(boxClass, repoBoxClass, regTrigProto, repoTrigProto);
	}
	public static <BT extends BoxImpl<TriggerImpl<BT>>> TriggerImpl<BT> makeTriggerPrototype(Class<BT> boxClass) {
		// The trigger subtype does not matter - what matters is capturing BT into the type.
		return new SysTriggers.QuitTrigger<BT>();
	}
	// static class ConcBootstrapTF extends BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
	// }  //   TT extends TriggerImpl<BT>
	public static <BT extends BoxImpl<TriggerImpl<BT>>, RBT extends RepoBoxImpl<TriggerImpl<RBT>>> BoxContextImpl 
				makeBoxContextImpl(Class<BT> regBoxClass, Class<RBT> repoBoxClass, TriggerImpl<BT> regTrigProto,
					TriggerImpl<RBT> repoTrigProto) {
		try {
			BoxContextImpl bctx = new BoxContextImpl();
			BT rootBox = DemoServiceWrapFuncs.makeTestBoxImpl(regBoxClass, regTrigProto, "rooty");
			bctx.contextualizeAndAttachRootBox(rootBox);

			BootstrapTriggerFactory btf = new BootstrapTriggerFactory();
			btf.attachTrigger(rootBox, new SysTriggers.QuitTrigger(), "quit");


			BT repoBox = DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, regBoxClass, regTrigProto, "repo");
			BT appBox = DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, regBoxClass, regTrigProto, "app");
			BT sysBox = DemoServiceWrapFuncs.makeTestChildBoxImpl(rootBox, regBoxClass, regTrigProto, "sys");

			RBT r1Box = DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox,  repoBoxClass, repoTrigProto, "h2.td_001");

			btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
			btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
			btf.attachTrigger(r1Box, new RepoTriggers.DumpStatsTrigger(), "dump stats");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", BoxPanel.Kind.REPO_MANAGER);

			RBT r2Box = DemoServiceWrapFuncs.makeTestChildBoxImpl(repoBox, repoBoxClass, repoTrigProto, "repo_002");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpD");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpC");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpA");

			BT fishBox = DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "fishy");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(fishBox, "open-matrix-f", BoxPanel.Kind.MATRIX);

			btf.attachTrigger(fishBox, new SysTriggers.DumpTrigger(), "dumpF");

			BT pumappBox = DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "pumapp");
			DemoServiceWrapFuncs.attachPanelOpenTrigger(pumappBox, "open-matrix-p", BoxPanel.Kind.MATRIX);
			btf.attachTrigger(pumappBox, new SysTriggers.DumpTrigger(), "dumpP");

			BT buckTreeBox = DemoServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "bucksum");
			btf.attachTrigger(buckTreeBox, new BridgeTriggers.MountSubmenuFromTriplesTrigger(), "loadSubmenus");
		
/*
			makeChildNode(appNode, "custy");
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
