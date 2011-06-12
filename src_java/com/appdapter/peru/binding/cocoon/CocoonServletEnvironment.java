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

package com.appdapter.peru.binding.cocoon;

import com.appdapter.peru.core.environment.Environment;
import com.appdapter.peru.core.environment.AbstractEnvironment;

import org.apache.avalon.framework.context.ContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class CocoonServletEnvironment extends AbstractEnvironment {
	private static Log 		theLog = LogFactory.getLog(CocoonServletEnvironment.class);
	
	private		String				myContextRootURL;
	
	public  CocoonServletEnvironment(org.apache.avalon.framework.context.Context ctx) throws ContextException {
		super();
		java.net.URL rootURL = (java.net.URL) ctx.get("context-root");
		myContextRootURL = rootURL.toExternalForm();
		theLog.info("%%%%%%%%%%%%%%%%%%%%  Found context-root: " + myContextRootURL);
	}
	public String resolveFilePath (String rawPath) throws Throwable {
		String result = myContextRootURL + rawPath;
		theLog.debug ("resolveFilePath() resolved " + rawPath + " to " + result);
		return result;
	}
}
