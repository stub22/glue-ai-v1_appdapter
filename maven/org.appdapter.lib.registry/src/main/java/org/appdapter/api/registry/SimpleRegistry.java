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
package org.appdapter.api.registry;

import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface SimpleRegistry extends Registry {

	public <OT> OT findRequiredUniqueMatch(Class<OT> objClaz, Pattern p) throws Exception;
	public <OT> OT findOptionalFirstMatch(Class<OT> objClaz, Pattern p);
	public <OT> OT findOptionalUniqueMatch(Class<OT> objClaz, Pattern p) throws Exception;
	
	public <OT> List<OT> findAllMatches(Class<OT> objClaz, Pattern p);	
}
