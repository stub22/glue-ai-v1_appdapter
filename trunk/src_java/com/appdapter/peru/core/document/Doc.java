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

import java.io.OutputStream;

import com.appdapter.peru.core.config.MutableConfig;
import com.appdapter.peru.core.name.Abbreviator;

import com.appdapter.peru.core.process.Data;


/**
 * Doc is currently hardwired to wrap around a dom4j doc.
 * This presents a certain handicap in representing small literals as Docs.
 * So, we plan to make the representation more flexible, later.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public abstract class Doc implements SentenceValue, Data {


	public abstract void writePretty (OutputStream outStream) throws Throwable;
	
	public abstract Doc transform(String xformPath, String systemID) throws Throwable;
	
	public abstract org.w3c.dom.Document getW3CDOM() throws Throwable;		
	
	public abstract void applyOverrides (MutableConfig conf, Abbreviator abbr) throws Throwable;

}
