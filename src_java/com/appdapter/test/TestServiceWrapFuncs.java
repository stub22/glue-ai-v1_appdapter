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
import com.appdapter.gui.trigger.PanelTriggers;

/**
 *
 * @author winston
 */
public class TestServiceWrapFuncs {
	public static <BoxType extends BoxImpl> BoxType makeTestBoxImpl(Class<BoxType> boxClass, String label) {
		BoxType result = CachingComponentAssembler.makeEmptyComponent(boxClass);
		result.setShortLabel(label);
		result.setDescription("full description for box with label: " + label);
		return result;
	}
	public static <BoxType extends BoxImpl> BoxType makeTestChildBoxImpl(Box parentBox, Class<BoxType> boxClass, String label) {
		BoxContext ctx = parentBox.getBoxContext();
		BoxType result = makeTestBoxImpl(boxClass, label);
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
