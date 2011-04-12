/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.trigger;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.MutableBox;
import com.appdapter.gui.box.MutableKnownComponent;
import com.appdapter.gui.box.MutableTrigger;
import com.appdapter.gui.box.Trigger;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 *
 * 	End run factory methods for bootstrap triggers (not read from config).
 */
public class BootstrapTriggerFactory<MBT extends MutableBox<? extends TT>, TT extends Trigger<? extends MBT> & MutableKnownComponent> {
	static Logger theLogger = LoggerFactory.getLogger(BootstrapTriggerFactory.class);

	// public  <BT extends Box<? extends TT>, TT extends MutableTrigger<BT> & MutableKnownComponent> TT
	<STT extends TT> STT putNewBootstrapTriggerOnBox(MutableBox<? super STT> mbox, Class<STT> trigClass, String trigName)  {
		STT trig = null;
		try {
			trig = trigClass.newInstance();
			trig.setShortLabel(trigName);
			mbox.attachTrigger(trig);

		} catch (Throwable t) {
			theLogger.error("Problem building trigger for class " + trigClass + " with name " + trigName, t);
		}
		return trig;
	}

}
