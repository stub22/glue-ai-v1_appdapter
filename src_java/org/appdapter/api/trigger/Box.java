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

import java.util.List;

import org.appdapter.core.debug.UIAnnotations.UIHidden;
import org.appdapter.core.jvm.GetObject;

/**
 * A Box is some entity displayable (perhaps in pieces) and interactable in GUI.
 * It is not necessarily a rectangular graphical area, although it might be.
 * Sometimes it is more like a wooden box of stuff, maybe a round one.
 *
 * @author Stu B. <www.texpedient.com>
 */
@UIHidden
public interface Box<TrigType extends Trigger<? extends Box<TrigType>>> extends GetObject {

	public BoxContext getBoxContext();

	public <T, E extends T> Iterable<E> getObjects(Class<T> type);

	public List<TrigType> getTriggers();

	//public Component getComponent();

	public Object getValue();

}
