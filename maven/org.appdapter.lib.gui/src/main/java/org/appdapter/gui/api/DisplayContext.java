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

package org.appdapter.gui.api;

import java.util.Collection;

import org.appdapter.api.trigger.UserResult;
import org.appdapter.gui.api.Ontologized.UIProvider;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface DisplayContext extends UIProvider/*, IShowObjectMessageAndErrors*/{
	// TODO : replace this with general BoxPanelSwitchableView
	public BoxPanelSwitchableView getBoxPanelTabPane();

	public NamedObjectCollection getLocalBoxedChildren();

	public Collection getTriggersFromUI(BT box, Object object);

	public UserResult attachChildUI(String title, Object value, boolean showASAP) throws Exception;

	public String getTitleOf(Object value);

}