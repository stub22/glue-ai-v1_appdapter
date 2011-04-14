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

package com.appdapter.gui.box;

import java.lang.Class;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
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
