/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.assembly;

import com.appdapter.core.item.Ident;
import com.appdapter.core.item.Item;
import com.appdapter.gui.box.MutableKnownComponent;
import com.appdapter.gui.box.TriggerImpl;

/**
 *
 * @author winston
 */
public abstract class DynamicCachingComponentAssembler<MKC extends MutableKnownComponent> extends CachingComponentAssembler<MKC> {
	@Override protected Class<MKC> decideComponentClass(Ident componentID, Item componentConfigItem) {
		String jfqcn = readConfigValString(componentID, AssemblyNames.P_javaFQCN, componentConfigItem, null);
		if (jfqcn != null) {
			theLogger.info("Found component class name: " + jfqcn);
			Class<MKC> triggerClass = findClass(jfqcn);
			return triggerClass;
		} else {
			throw new RuntimeException("Cannot find class name for componentID: " + componentID);
		}
	}
	public Class<MKC> findClass(String btcFQCN) {
		Class c = null;
		try {
			c = Class.forName(btcFQCN);
		} catch (Throwable t) {
			theLogger.error("Problem looking up class " + btcFQCN, t);
		}
		return c;
	}
}
