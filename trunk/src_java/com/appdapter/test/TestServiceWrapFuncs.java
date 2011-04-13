/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.test;

import com.appdapter.gui.assembly.CachingComponentAssembler;
import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.BoxContext;
import com.appdapter.gui.box.BoxImpl;
import com.appdapter.gui.box.BoxPanel;
import com.appdapter.gui.box.MutableBox;
import com.appdapter.gui.box.TriggerImpl;
import com.appdapter.gui.trigger.PanelTriggers;

/**
 *
 * @author winston
 */
public class TestServiceWrapFuncs {
	public static <BT extends BoxImpl<TT>, TT extends TriggerImpl<BT>> BT makeTestBoxImpl(Class<BT> boxClass, Class<TT> trigClass, String label) {
		BT result = CachingComponentAssembler.makeEmptyComponent(boxClass);
		result.setShortLabel(label);
		result.setDescription("full description for box with label: " + label);
		return result;
	}
	public static <BT extends BoxImpl<TT>, TT extends TriggerImpl<BT>> BT makeTestBoxImpl(Class<BT> boxClass, TT trigProto, String label) {
		BT result = CachingComponentAssembler.makeEmptyComponent(boxClass);
		result.setShortLabel(label);
		result.setDescription("full description for box with label: " + label);
		return result;
	}
	public static <BT extends BoxImpl<TT>, TT extends TriggerImpl<BT>> BT makeTestChildBoxImpl(Box parentBox, Class<BT> boxClass,  TT trigProto, String label) {
		BT result = null;
		BoxContext ctx = parentBox.getBoxContext();
		result = makeTestBoxImpl(boxClass, trigProto, label);
		ctx.contextualizeAndAttachChildBox(parentBox, result);
		return result;
	}

	public static PanelTriggers.OpenTrigger attachPanelOpenTrigger(MutableBox box, String label, BoxPanel.Kind kind) {
		PanelTriggers.OpenTrigger trig = new PanelTriggers.OpenTrigger();
		trig.setShortLabel(label);
		trig.setPanelKind(kind);
		box.attachTrigger(trig);
		return trig;
	}
/*
	public static BoxTreeNode makeNode(BoxContext bc, String label) {
		BoxImpl bi = new BoxImpl(label);
		bi.setContext(bc);
		BoxTreeNode btn = new BoxTreeNode(bi);
		bi.setDisplayContextProvider(btn);
		return btn;
	}
	public static BoxTreeNode makeChildNode(BoxTreeNode parentNode, String label) {
		BoxContext bctx = parentNode.getBox().getContext();
		BoxTreeNode childNode = makeNode(bctx, label);
		parentNode.add(childNode);
		return childNode;
	}
 *
 */
	/*
	public static BoxImpl boxImpl(BoxTreeNode btn) {
		return (BoxImpl) btn.getBox();
	}
	 *
	 */
}
