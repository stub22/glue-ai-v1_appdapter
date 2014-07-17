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

package org.appdapter.peru.core.document;

import org.appdapter.peru.core.config.Config;

/**
 * A SentenceValue is something that can format itself for use as representation of a
 * value used in a logical sentence in some category of Configs.  For example, if this 
 * SV can produce an RDF-Literal, then it can be used to produce such a literal for any 
 * RDF model mentioned by a config.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public interface SentenceValue {
	public Object getCompatibleValue (Config conf) throws Throwable;
}
