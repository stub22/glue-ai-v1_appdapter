/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.assembly;

import com.appdapter.core.item.Ident;
import com.appdapter.core.item.Item;
import com.appdapter.gui.box.TriggerImpl;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 */
public class TriggerAssembler extends DynamicCachingComponentAssembler<TriggerImpl> {
	static Logger theLogger = LoggerFactory.getLogger(TriggerAssembler.class);
/*
	@Override protected Class<TriggerImpl> decideComponentClass(Ident componentID, Item componentConfigItem) {
		String jfqcn = readConfigValString(componentID, AssemblyNames.P_javaFQCN, componentConfigItem, null);
		if (jfqcn != null) {
			theLogger.info("Found trigger class name: " + jfqcn);
			Class<TriggerImpl> triggerClass = TriggerImpl.findBoxTriggerClass(jfqcn);
			return triggerClass;
		} else {
			throw new RuntimeException("Cannot find class name for trigger with componentID: " + componentID);
		}
	}
 * 
 */
	@Override protected void initExtendedFieldsAndLinks(TriggerImpl comp, Item configItem, Assembler asmblr, Mode mode) {
		theLogger.info("bonus trigger init here");
		Ident compID = comp.getIdent();
		// TriggerImpl	bt = TriggerImpl.attachNoargsTrigger(mbox, btClass, apsbLabel);
	}

}
