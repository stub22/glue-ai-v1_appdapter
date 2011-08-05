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

package org.appdapter.gui.main;

import org.appdapter.gui.box.Box;
import org.appdapter.gui.assembly.CachingComponentAssembler;
import org.appdapter.gui.box.BoxContext;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.BoxPanel;
import org.appdapter.gui.box.MutableBox;
import org.appdapter.gui.box.TriggerImpl;
import org.appdapter.gui.trigger.PanelTriggers;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TestServiceWrapFuncs {
	private static <BT extends BoxImpl<TT>, TT extends TriggerImpl<BT>> BT makeTestBoxImpl(Class<BT> boxClass, Class<TT> trigClass, String label) {
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
