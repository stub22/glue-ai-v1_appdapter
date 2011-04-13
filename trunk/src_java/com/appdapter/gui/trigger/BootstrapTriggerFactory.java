/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.trigger;

import com.appdapter.gui.box.BoxImpl;
import com.appdapter.gui.box.MutableBox;
import com.appdapter.gui.box.TriggerImpl;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 *
 * 	End run factory methods for bootstrap triggers (not read from config).
 * // MBT extends MutableBox<? extends TT>,
 */
public class BootstrapTriggerFactory { // <TT extends Trigger<? extends MutableBox<? extends TT>> & MutableKnownComponent> {
	static Logger theLogger = LoggerFactory.getLogger(BootstrapTriggerFactory.class);

	// public  <BT extends Box<? extends TT>, TT extends MutableTrigger<BT> & MutableKnownComponent> TT
	<CTT extends ConcreteTrigger> CTT putNewConcreteTriggerOnBox(MutableBox<ConcreteTrigger> mbox, Class<CTT> trigClass, String trigName)  {
		CTT trig = null;
		try {
			trig = trigClass.newInstance();
			trig.setShortLabel(trigName);
			mbox.attachTrigger(trig);

		} catch (Throwable t) {
			theLogger.error("Problem building trigger for class " + trigClass + " with name " + trigName, t);
		}
		return trig;
	}
	public <RTT extends TriggerImpl<BoxImpl<RTT>>> RTT putTriggerOnBox(MutableBox<? super RTT> mbox, Class<RTT> trigClass, String trigName) {
		return null;
	}
	public <BT extends BoxImpl<TriggerImpl<BT>>> TriggerImpl<BT> attachNewTrigger(BT box, Class<? extends TriggerImpl<BT>> trigClass,  String trigName) {
		TriggerImpl<BT> trig = null;
		try {
			trig = trigClass.newInstance();
			attachTrigger(box, trig, trigName);
		} catch (Throwable t) {
			theLogger.error("Problem building trigger for class " + trigClass + " with name " + trigName, t);
		}
		return trig;
	}
	public <BT extends BoxImpl<TriggerImpl<BT>>> void attachTrigger(BT box, TriggerImpl<BT> trigger,  String trigName) {
		trigger.setShortLabel(trigName);
		box.attachTrigger(trigger);
	}
}
