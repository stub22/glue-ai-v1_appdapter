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

import javax.swing.*;


/**
 * BoxPanels may be used to view many different boxes.
 * The Kind-enum is a placeholder until the GUI browser is fully ontologized.
 * Meanwhile, we can plug in anything as "OTHER".
 * 
 * @author Stu B. <www.texpedient.com>
 */
public abstract class ScreenBoxPanel<BoxType extends Box> extends JPanel {
	public enum Kind {
		MATRIX, DB_MANAGER, REPO_MANAGER, OBJECT_PROPERTIES, OTHER
	}

	/** Make the display of this panel foocus on a particular box.
	 * 
	 * @param b - a box to focus on
	 */
	public abstract void focusOnBox(BoxType b);

	/** Return the live object in which we think we are updating 
	 * 
	 *  This can be 'this' object
	 * 
	 */
	abstract public Object getObject();
}
