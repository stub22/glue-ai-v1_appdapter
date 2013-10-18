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

package org.appdapter.peru.binding.servlet;

import javax.servlet.ServletContext;

import org.appdapter.peru.core.environment.AbstractEnvironment;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class ServletEnvironment extends AbstractEnvironment {
	private 	ServletContext		myServletContext;
	public  ServletEnvironment(ServletContext servletContext) {
		myServletContext = servletContext;
	}
	public String resolveFilePath (String rawPath) throws Throwable {
		return myServletContext.getRealPath(rawPath);
	}
}

