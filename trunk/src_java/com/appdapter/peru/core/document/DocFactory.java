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

package com.appdapter.peru.core.document;


/**
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public  class DocFactory {

	public static Doc makeDocFromW3CDOM(org.w3c.dom.Document w3cDOM) throws Throwable {
		return com.appdapter.peru.binding.dom4j.Dom4jDoc.buildFromW3CDOM(w3cDOM);
	}
	
	public static Doc makeDocFromObject(Object input, boolean throwOnFail) throws Throwable {
		Doc	resultD = null;
		
		if (input instanceof Doc) {
			resultD = (Doc) input;
		} else if (input instanceof org.w3c.dom.Document) {
			org.w3c.dom.Document inDocW3C = (org.w3c.dom.Document) input;
			resultD = makeDocFromW3CDOM(inDocW3C);
		}
		if ((resultD == null) && throwOnFail) {
			throw new Exception("Cannot make net.peruser.core.document.Doc from " + input);
		}
		return resultD;
	}
}
