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
import java.io.FileInputStream;

import org.appdapter.peru.core.handle.HandleDirectory;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public abstract class AbstractEnvironment implements Environment {
	
	private		HandleDirectory		myPrimaryHandleDirectory;
	
	public AbstractEnvironment() { }
	
	public AbstractEnvironment(Environment parent) throws Throwable {
		HandleDirectory parentDirectory = parent.getPrimaryHandleDirectory();
		myPrimaryHandleDirectory = parentDirectory;
	}
	
	/*
	 * This implementation uses "resolveFilePath()", and assumes the path is openable with FileInputStream.
	 */
	public InputStream openStream (String canonPath) throws Throwable {
		return new FileInputStream(resolveFilePath(canonPath));
	}
	/**
	 */
	public HandleDirectory getPrimaryHandleDirectory() throws Throwable {
		if (myPrimaryHandleDirectory == null) {
			myPrimaryHandleDirectory = HandleDirectory.getDefaultDirectory();
		}
		return myPrimaryHandleDirectory;
	}
	/**
	 */
	public void setPrimaryHandleDirectory (HandleDirectory hd) throws Throwable {
		if (myPrimaryHandleDirectory != null) {
			throw new Exception("ILLEGAL PERUSER OPERATION: Cannot reset primaryHandleDirectory for environment " + this 
				+ " in peruser version + @PERUSER_VERSION_FROM_ANNOTATION");

		}
		myPrimaryHandleDirectory = hd;
	}
	
}

