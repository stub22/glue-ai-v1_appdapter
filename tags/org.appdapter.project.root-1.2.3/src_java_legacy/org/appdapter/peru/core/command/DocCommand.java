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
import org.appdapter.peru.core.document.DocFactory;

// import org.dom4j.Document;

import org.appdapter.peru.core.environment.Environment;

import org.appdapter.peru.core.name.Address;

import static org.appdapter.peru.core.vocabulary.SubstrateAddressConstants.*;
 
/** DocCommand is a Command that knows how to process docs, which can be either
 *  Peruser docs or W3C-DOM docs.  
 * The output type is always the same as the input type.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public abstract class DocCommand extends AbstractCommand {

	public Object work(Object input) throws Throwable {
		Object result = null;
		if (input instanceof Doc) {
			Doc inDoc = (Doc) input; 
			Doc outDoc = workDoc(inDoc);
			result = outDoc;
		} else if (input instanceof org.w3c.dom.Document) {
			org.w3c.dom.Document   inW3 = (org.w3c.dom.Document) input;
			org.w3c.dom.Document   outW3 = workW3CDOM(inW3);
			result = outW3;
		}
		return result;
	}	
	/**
	  * Execute the one and only document transaction that this command exists to process,
	  * making any necessary changes to stored models, docs, etc.
	  */
	  
	protected abstract Doc workDoc(Doc input) throws Throwable;
	
		/**
	 * Implementation of this method depends on workDoc() being implemented.
	 * Process a w3c DOM doc (insert exact version?) to produce another w3c DOM doc.  Same semantic role as "process()".
	 * <br/>	 
	 */
	 
	private org.w3c.dom.Document workW3CDOM (org.w3c.dom.Document inDocW3C) throws Throwable {
		org.w3c.dom.Document outDocW3C;

		// Fixme
		Doc pd = DocFactory.makeDocFromW3CDOM(inDocW3C);
		
		Doc resultDoc = this.workDoc(pd);

		outDocW3C = resultDoc.getW3CDOM();
		return outDocW3C;
	}
	
	/**
	  *  Find a doc transform 
	  */
	public String getTransformPath () throws Throwable {
		String xformPath = null;
		Address cmdAddress = getCommandAddress();
		Address xformConfig = myConfigImpl.getOptionalAddress(cmdAddress, transformConfigPropAddress);
		if (xformConfig != null) {
			String rawXformPath = myConfigImpl.getSingleString(xformConfig, transformPathPropAddress);
			xformPath = mapPlaceURL(rawXformPath);
		}
		return xformPath;
	}	

	/*
			// This extra transform stuff is not well-grounded yet...Note the heinous cast to AbstractCommand
		String xformPath = ((AbstractCommand) command).getTransformPath();
		System.out.println("transformPath is " + xformPath);
		
		if (xformPath == null) {
			formalResult = commandResult;
		} else {
			// String realXformPath = getServletContext().getRealPath(xformPath);
			formalResult = (ResultDoc) commandResult.transform(xformPath, xformPath);
		} 
		return formalResult;
	*/
	
}
