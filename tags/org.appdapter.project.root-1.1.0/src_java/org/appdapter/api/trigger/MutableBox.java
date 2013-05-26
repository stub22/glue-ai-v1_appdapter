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

import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.Box;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface MutableBox<TrigType extends Trigger<? extends MutableBox<TrigType>>> extends Box<TrigType>  {

	void attachTrigger(TrigType bt);
	void clearTriggers();
	void setContext(BoxContext bc);

}