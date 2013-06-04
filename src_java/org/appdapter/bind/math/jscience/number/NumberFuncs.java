/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.appdapter.bind.math.jscience.number;
import java.util.List;
import java.util.logging.Level;

import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Number;
import org.slf4j.Logger;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class NumberFuncs {
	public static <NT extends Number<NT>> NT evalPoly(Polynomial<NT> poly) {
		return evalPrintReturnPoly(poly, null, null, null, false);
	}
	public static <NT extends Number<NT>> NT evalPrintReturnPoly(Polynomial<NT> poly, String logLabel,
				Logger logger, Level logLev, boolean logFlag) {
		NT nval = poly.evaluate();
// 		if (logFlag && logger.isLoggable(logLev)) {
		if (logFlag) {
			StringBuffer msg = new StringBuffer(logLabel).append(" = {");
			msg.append(poly.toString()).append("} (").append(dumpPolyVars(poly)).append(") = ").append(nval);
			logger.info(msg.toString());
//			logger.log(logLev, msg.toString());
		}
		return nval;
	}
	public static <NT extends Number<NT>> String dumpPolyVars(Polynomial<NT> poly) {
		StringBuffer buf = new StringBuffer("[");
		List<Variable<NT>> polyVars = poly.getVariables();
		boolean firstVar = true;
		for (Variable<NT> v : polyVars) {
			String sym = v.getSymbol();
			NT nval = v.get();
			if (!firstVar) {
				buf.append(",");
			}
			buf.append(sym).append("=").append(nval.toString());
			firstVar = false;
		}
		buf.append("]");
		return buf.toString();
	}
}
