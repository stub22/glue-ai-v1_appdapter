/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.test;

import com.appdapter.gui.box.Box;
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
		BoxContextImpl bctx = makeBoxContextImpl();
		TreeModel tm = bctx.getTreeModel();
		BoxTreeNode rootBTN = (BoxTreeNode) tm.getRoot();

		DisplayContextProvider dcp = bctx;
		TestNavigatorCtrl tn = new TestNavigatorCtrl(bctx, tm, rootBTN, dcp);
		return tn;
	}
		
	public static BoxContextImpl makeBoxContextImpl() {
		try {
			BoxContextImpl bctx = new BoxContextImpl();
			BoxImpl rootBox = TestServiceWrapFuncs.makeTestBoxImpl(BoxImpl.class, "rooty");
			bctx.contextualizeAndAttachRootBox(rootBox);

			// BootstrapTriggerFactory<BoxImpl<TriggerImpl>, TriggerImpl<BoxImpl>> btf;
						// = new BootstrapTriggerFactory<BoxImpl<TriggerImpl>, TriggerImpl<BoxImpl>> ();
			/*
			TriggerImpl.putNewHardwiredTriggerOnBox(rootBox, SysTriggers.QuitTrigger.class, "quit");

			Box repoBox = TestServiceWrapFuncs.makeTestChildBoxImpl(rootBox, BoxImpl.class, "repo");
			Box appBox = TestServiceWrapFuncs.makeTestChildBoxImpl(rootBox, BoxImpl.class, "app");
			Box sysBox = TestServiceWrapFuncs.makeTestChildBoxImpl(rootBox, BoxImpl.class, "sys");

			BoxImpl r1Box = TestServiceWrapFuncs.makeTestChildBoxImpl(repoBox, RepoBoxImpl.class, "h2.td_001");
			TriggerImpl.putNewHardwiredTriggerOnBox(r1Box, DatabaseTriggers.InitTrigger.class, "openDB");
			TriggerImpl.putNewHardwiredTriggerOnBox(r1Box, RepoTriggers.OpenTrigger.class, "openMetaRepo");
			TriggerImpl.putNewHardwiredTriggerOnBox(r1Box, RepoTriggers.InitTrigger.class, "initMetaRepo");
			TriggerImpl.putNewHardwiredTriggerOnBox(r1Box, RepoTriggers.UploadTrigger.class, "upload into MetaRepo");
			TriggerImpl.putNewHardwiredTriggerOnBox(r1Box, RepoTriggers.QueryTrigger.class, "query repo");
			TriggerImpl.putNewHardwiredTriggerOnBox(r1Box, RepoTriggers.DumpStatsTrigger.class, "dump stats");
			TestServiceWrapFuncs.attachPanelOpenTrigger(r1Box, "manage repo", BoxPanel.Kind.REPO_MANAGER);

			BoxImpl r2Box = TestServiceWrapFuncs.makeTestChildBoxImpl(repoBox, RepoBoxImpl.class, "repo_002");
			TriggerImpl.putNewHardwiredTriggerOnBox(r2Box, SysTriggers.DumpTrigger.class, "dumpD");
			TriggerImpl.putNewHardwiredTriggerOnBox(r2Box, SysTriggers.DumpTrigger.class, "dumpC");
			TriggerImpl.putNewHardwiredTriggerOnBox(r2Box, SysTriggers.DumpTrigger.class, "dumpA");

			BoxImpl fishBox = TestServiceWrapFuncs.makeTestChildBoxImpl(appBox, BoxImpl.class, "fishy");
			TestServiceWrapFuncs.attachPanelOpenTrigger(fishBox, "open-matrix-f", BoxPanel.Kind.MATRIX);

			TriggerImpl.putNewHardwiredTriggerOnBox(fishBox, SysTriggers.DumpTrigger.class, "dumpF");

			BoxImpl pumappBox = TestServiceWrapFuncs.makeTestChildBoxImpl(appBox, BoxImpl.class, "pumapp");
			TestServiceWrapFuncs.attachPanelOpenTrigger(pumappBox, "open-matrix-p", BoxPanel.Kind.MATRIX);
			TriggerImpl.putNewHardwiredTriggerOnBox(pumappBox, SysTriggers.DumpTrigger.class, "dumpP");

			BoxImpl buckTreeBox = TestServiceWrapFuncs.makeTestChildBoxImpl(appBox, BoxImpl.class, "bucksum");
			TriggerImpl.putNewHardwiredTriggerOnBox(buckTreeBox, BridgeTriggers.MountSubmenuFromTriplesTrigger.class, "loadSubmenus");
			 * 
			 */
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
