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


package org.appdapter.peru.core.command;

import org.appdapter.peru.core.config.Config;

import org.appdapter.peru.core.document.Doc;

import java.util.Map;

import org.appdapter.peru.core.environment.Environment;

import org.appdapter.peru.core.name.Address;
 
/** MapCommand is a Command that knows how to process java.util.Map objects.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public abstract class MapCommand extends AbstractCommand {

	public Object work(Object input) throws Throwable {
		Object result = null;
		if (input instanceof Map) {
			Map	inMap = (Map) input; 
			Map outMap = workMap(inMap);
			result = outMap;
		} 
		return result;
	}	
	/**
	  * Execute the one and only Map-based operation that this command exists to process,
	  * making any necessary changes to stored models, docs, etc.
	  */
	  
	protected abstract Map workMap(Map input) throws Throwable;
}
