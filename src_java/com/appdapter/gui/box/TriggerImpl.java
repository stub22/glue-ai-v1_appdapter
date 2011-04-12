/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import java.lang.Class;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 */
public abstract class TriggerImpl<BoxType extends Box<? extends TriggerImpl<BoxType>>> extends KnownComponentImpl implements MutableTrigger<BoxType> {
	static Logger theLogger = LoggerFactory.getLogger(TriggerImpl.class);

	/*

	public static <BT extends Box<? extends TT>, TT extends MutableTrigger<BT> & MutableKnownComponent> TT putNewHardwiredTriggerOnBox(MutableBox<TT> mbox, Class<TT> trigClass, String trigName)  {
		TT trig = null;
		try {
			trig = trigClass.newInstance();
			trig.setShortLabel(trigName);
			mbox.attachTrigger(trig);

		} catch (Throwable t) {
			theLogger.error("Problem building trigger for class " + trigClass + " with name " + trigName, t);
		}
		return trig;
	}
	 *
	 */
	/*
	public static Class<TriggerImpl> findBoxTriggerClass(String btcFQCN) {
		Class c = null;
		try {
			c = Class.forName(btcFQCN);
		} catch (Throwable t) {
			theLogger.error("Problem looking up class " + btcFQCN, t);
		}
		return c;
	}
	*/
	@Override protected String getFieldSummary() {
		return "trigger-field-summary-goes-here";
	}

}
