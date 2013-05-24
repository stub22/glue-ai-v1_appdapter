/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.api.trigger;

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.component.KnownComponentImpl;


/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BoxImpl <TrigType extends Trigger<? extends BoxImpl<TrigType>>> extends KnownComponentImpl 
			implements MutableBox<TrigType> {
	
	private	BoxContext						myBoxContext;

	private List<TrigType>				myTriggers = new ArrayList<TrigType>();
	
	@Override public void setContext(BoxContext bc) {
		myBoxContext = bc;
	}
	@Override public BoxContext getBoxContext() {
		return myBoxContext;
	}
	@Override public void clearTriggers() {
		myTriggers.clear();
	}
	@Override public void attachTrigger(TrigType trig) {
		myTriggers.add(trig);
	}
	@Override public List<TrigType> getTriggers() {
		return myTriggers;
	}
	@Override public String getFieldSummary() {
		return super.getFieldSummary() + ", triggerCount=" + myTriggers.size();
	}
	
}
