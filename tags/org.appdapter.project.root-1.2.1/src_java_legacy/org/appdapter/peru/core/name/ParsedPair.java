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

package org.appdapter.peru.core.name;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class ParsedPair {
	public String	left, right;

	public static ParsedPair parsePair(String unparsed, String splitter) {
		ParsedPair pp = new ParsedPair();
		int		splitterIndex = unparsed.indexOf(splitter);
		String prefix = null, ident;
		if (splitterIndex != -1) {
			pp.left = unparsed.substring(0, splitterIndex);
			pp.right = unparsed.substring(splitterIndex + splitter.length());
		} else {
			pp = null;
			// throw new Exception ("pair-string " + unparsed + " does not contain expected splitter " + splitter);
		}
		return pp;
	}
}

