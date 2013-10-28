/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.math.jscience.function;
import java.util.List;

import org.jscience.mathematics.function.Variable;
// import org.jscience.mathematics.number.Number;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class VariableFuncs {

	public static <NT> String dumpVarList(List<Variable<NT>> vars) {
		if (vars == null) {return null;}
		StringBuffer buf = new StringBuffer("[");
		boolean firstVar = true;
		for (Variable<NT> v : vars) {
			String sym = v.getSymbol();
			NT nval = v.get();
			if (!firstVar) {
				buf.append(",");
			}
			buf.append(sym).append("=").append(nval);
			firstVar = false;
		}
		buf.append("]");
		return buf.toString();
	}
}
