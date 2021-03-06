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

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BoxContextImpl implements BoxContext {
	public <BT extends Box<TT>, TT extends Trigger<BT>> List<BT> getOpenChildBoxesNarrowed(Box parent, Class<BT> boxClass, Class<TT> trigClass) {
		List<Box> wideOpenChildBoxes = getOpenChildBoxes(parent);
		List<BT> narrowedOpenChildBoxes = new ArrayList<BT>();
		for (Box wocb : wideOpenChildBoxes) {
			// This cast does not do any actual runtime typechecking under JDK 1.6.
			BT narrow = (BT) wocb;
			narrowedOpenChildBoxes.add(narrow);
		}
		return narrowedOpenChildBoxes;
	}
}
