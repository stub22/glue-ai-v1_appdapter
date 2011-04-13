/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.test;

import com.appdapter.gui.box.BoxContextImpl;
import com.appdapter.gui.box.BoxImpl;
import com.appdapter.gui.box.BoxPanel;
import com.appdapter.gui.box.BoxTreeNode;
import com.appdapter.gui.box.DisplayContextProvider;
import com.appdapter.gui.box.TriggerImpl;
import com.appdapter.gui.repo.RepoBoxImpl;
import com.appdapter.gui.trigger.BootstrapTriggerFactory;
import com.appdapter.gui.trigger.BridgeTriggers;
import com.appdapter.gui.trigger.DatabaseTriggers;
import com.appdapter.gui.trigger.RepoTriggers;
import com.appdapter.gui.trigger.SysTriggers;
import javax.swing.tree.TreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 */
public class TestBrowse {
	static Logger theLogger = LoggerFactory.getLogger(TestBrowse.class);

	public static void pretendToBeAwesome() {
		System.out.println("pretendToBeAwesome - Printing to stdout!");
		theLogger.info("pretendToBeAwesome - Printing info message to logger!");
	}
	public static void main(String[] args) {
		pretendToBeAwesome();
		theLogger.info("TestBrowse.main()-START");
		TestNavigatorCtrl tn = makeTestNavigatorCtrl(args);
		tn.launchFrame("Appdap Refact");
	}
	public static TestNavigatorCtrl makeTestNavigatorCtrl(String[] args) {
		// From this BoxImpl.class, is makeBCI is able to infer the full BT=BoxImpl<... tree?
		BoxContextImpl bctx = makeBCI(BoxImpl.class, RepoBoxImpl.class);// makeBoxContextImpl(BoxImpl.class, TriggerImpl.class);
		TreeModel tm = bctx.getTreeModel();
		BoxTreeNode rootBTN = (BoxTreeNode) tm.getRoot();

		DisplayContextProvider dcp = bctx;
		TestNavigatorCtrl tn = new TestNavigatorCtrl(bctx, tm, rootBTN, dcp);
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
			BT rootBox = TestServiceWrapFuncs.makeTestBoxImpl(regBoxClass, regTrigProto, "rooty");
			bctx.contextualizeAndAttachRootBox(rootBox);

			BootstrapTriggerFactory btf = new BootstrapTriggerFactory();
			btf.attachTrigger(rootBox, new SysTriggers.QuitTrigger(), "quit");


			BT repoBox = TestServiceWrapFuncs.makeTestChildBoxImpl(rootBox, regBoxClass, regTrigProto, "repo");
			BT appBox = TestServiceWrapFuncs.makeTestChildBoxImpl(rootBox, regBoxClass, regTrigProto, "app");
			BT sysBox = TestServiceWrapFuncs.makeTestChildBoxImpl(rootBox, regBoxClass, regTrigProto, "sys");

			RBT r1Box = TestServiceWrapFuncs.makeTestChildBoxImpl(repoBox,  repoBoxClass, repoTrigProto, "h2.td_001");

			btf.attachTrigger(r1Box, new DatabaseTriggers.InitTrigger(), "openDB");
			btf.attachTrigger(r1Box, new RepoTriggers.OpenTrigger(), "openMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.InitTrigger(), "initMetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.UploadTrigger(), "upload into MetaRepo");
			btf.attachTrigger(r1Box, new RepoTriggers.QueryTrigger(), "query repo");
			btf.attachTrigger(r1Box, new RepoTriggers.DumpStatsTrigger(), "dump stats");
			TestServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", BoxPanel.Kind.REPO_MANAGER);

			RBT r2Box = TestServiceWrapFuncs.makeTestChildBoxImpl(repoBox, repoBoxClass, repoTrigProto, "repo_002");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpD");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpC");
			btf.attachTrigger(r2Box, new SysTriggers.DumpTrigger(), "dumpA");

			BT fishBox = TestServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "fishy");
			TestServiceWrapFuncs.attachPanelOpenTrigger(fishBox, "open-matrix-f", BoxPanel.Kind.MATRIX);

			btf.attachTrigger(fishBox, new SysTriggers.DumpTrigger(), "dumpF");

			BT pumappBox = TestServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "pumapp");
			TestServiceWrapFuncs.attachPanelOpenTrigger(pumappBox, "open-matrix-p", BoxPanel.Kind.MATRIX);
			btf.attachTrigger(pumappBox, new SysTriggers.DumpTrigger(), "dumpP");

			BT buckTreeBox = TestServiceWrapFuncs.makeTestChildBoxImpl(appBox, regBoxClass, regTrigProto, "bucksum");
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
