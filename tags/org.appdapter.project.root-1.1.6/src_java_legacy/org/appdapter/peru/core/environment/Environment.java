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

package org.appdapter.peru.core.environment;

import java.io.InputStream;

import org.appdapter.peru.core.handle.HandleDirectory;


/** 	An Environment provides access to system resources for processing Machines.
 * 
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public interface Environment {
	
	/** The resolution mechanism depends on how the Peruser Bindings are set up for this Environment.
	  * @param canonPath	A forward-slash style relative path/like/this.whatever
	  * @return             A system path that can be used to open the resource in this JVM + Peruser.
	  */
	public String resolveFilePath(String canonPath) throws Throwable;
	
	/** Convenience method to call resolveFilePath and then open a JVM InputStream on the result.
	  * @param canonPath	A forward-slash style relative path/like/this.whatever
	  * @return             A stream open to the resource.
	  */
	public InputStream openStream(String canonPath) throws Throwable;
	
	/**
	  *  Return the "primary" directory of handles "known" in this environment. 
	  */
	public HandleDirectory getPrimaryHandleDirectory() throws Throwable;
	 
}
