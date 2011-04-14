/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.gui.assembly;

import com.appdapter.core.item.Ident;
import com.appdapter.core.item.Item;
import com.appdapter.gui.box.TriggerImpl;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
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
