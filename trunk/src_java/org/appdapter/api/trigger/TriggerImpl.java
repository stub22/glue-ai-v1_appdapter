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

package org.appdapter.api.trigger;

import org.appdapter.api.trigger.MutableTrigger;
import org.appdapter.api.trigger.Box;
import org.appdapter.core.component.KnownComponentImpl;


/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class TriggerImpl<BoxType extends Box<? extends TriggerImpl<BoxType>>> extends KnownComponentImpl implements MutableTrigger<BoxType> {

	@Override public String getFieldSummary() {
		return super.getFieldSummary() +  ", trigger-field-summary-goes-here";
	}

}
