/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.assembly;

import com.appdapter.core.item.Ident;
import com.appdapter.core.item.Item;
import com.appdapter.gui.box.BoxImpl;
import com.appdapter.gui.box.Trigger;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author winston
 */
public class BoxAssembler extends DynamicCachingComponentAssembler<BoxImpl> {
	static Logger theLogger = LoggerFactory.getLogger(BoxAssembler.class);

/*
	@Override protected Class<BoxImpl> decideComponentClass(Ident componentID, Item componentConfigItem) {
		return BoxImpl.class;
	}
*/
	@Override protected void initExtendedFieldsAndLinks(BoxImpl box, Item configItem, Assembler asmblr, Mode mode) {
		theLogger.info("bonus box init here");
		List<Object> linkedTriggers = findOrMakeLinkedObjects(configItem, AssemblyNames.P_trigger, asmblr, mode, null);
		for (Object lt : linkedTriggers) {
			Trigger t = (Trigger) lt;
			box.attachTrigger(t);
		}
	}

}
