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
import org.jscience.mathematics.function.Term;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Real;
import org.slf4j.Logger;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class RealFuncs  {


	public static Real makeReal(double d) {
		// Real.valueOf seems to have a bug when dealing with negative inputs!
		double mag = Math.abs(d);
		throw new RuntimeException("need to replace code for Real.valueOf()");
		// Real magReal  = Real.valueOf(mag);
		/*
		if (d < 0.0) {
			return magReal.opposite();
		} else {
			return magReal;
		}
		 * 
		 */
	}
	public static void setVariableValue(Variable<Real> var, double value) {
		Real rval = makeReal(value);
		var.set(rval);
	}
	public static double getVariableValue(Variable<Real> var) {
		Real rval = var.get();
		if (rval == null) {
			throw new RuntimeException ("Null value for var with sym=" + var.getSymbol());
		}
		return rval.doubleValue();
	}
	public static void setInputVarValue(Polynomial poly, String fullSymbol, double value) {
		Variable<Real> v = poly.getVariable(fullSymbol);
		if (v == null) {
			throw new RuntimeException ("Can't locate var for sym=" + fullSymbol + " in poly=" + poly);
		}
		setVariableValue(v, value);
	}
	public static double getInputVarValue(Polynomial poly, String fullSymbol) {
		Variable<Real> v = poly.getVariable(fullSymbol);
		if (v == null) {
			throw new RuntimeException ("Can't locate var for sym=" + fullSymbol + " in poly=" + poly);
		}
		Real rval = v.get();
		if (rval == null) {
			throw new RuntimeException ("Null value for var with sym=" + fullSymbol + " in poly=" + poly);
		}
		return rval.doubleValue();
	}	
	public static double evalPoly(Polynomial<Real> poly) {
		return evalPrintReturnPoly(poly, null, null, null, false);
	}
	public static double evalPrintReturnPoly(Polynomial<Real> poly, String logLabel,
				Logger logger, Level logLev, boolean logFlag) {
		Real val = poly.evaluate();
		if (logFlag) { // (logFlag && logger.isLoggable(logLev)) {
			StringBuffer msg = new StringBuffer(logLabel).append(" = {");
			msg.append(poly.toString()).append("} (").append(dumpPolyVars(poly)).append(") = ").append(val);
			// logger.log(logLev, msg.toString());
			logger.info(msg.toString());
		}
		return val.doubleValue();
	}
	public static String dumpPolyVars(Polynomial poly) {
		StringBuffer buf = new StringBuffer("[");
		List<Variable<Real>> polyVars = poly.getVariables();
		boolean firstVar = true;
		for (Variable<Real> v : polyVars) {
			String sym = v.getSymbol();
			Real rval = v.get();
			if (!firstVar) {
				buf.append(",");
			}
			buf.append(sym).append("=").append(rval.toString());
			firstVar = false;
		}
		buf.append("]");
		return buf.toString();
	}

	public static Polynomial makeConstAccelPosPoly(Variable<Real> rangeOffsetTimeVar,
				Variable<Real> rangeAccelVar,
				Variable<Real> rangePosStartVar,
				Variable<Real> rangeVelStartVar) {
		// x = x0 + v0t + .5 a t^2

		Term  timeSquaredTerm = Term.valueOf(rangeOffsetTimeVar, 2);
		Term  accelTerm = Term.valueOf(rangeAccelVar, 1);
		Term  accelTimeSqTerm = timeSquaredTerm.times(accelTerm);

		Real oneHalf = RealFuncs.makeReal(0.5);

		Polynomial parabPoly = Polynomial.valueOf(oneHalf, accelTimeSqTerm);

		Term timeTerm = Term.valueOf(rangeOffsetTimeVar, 1);
		Term velTerm = Term.valueOf(rangeVelStartVar, 1);
		Term velTimeTerm = timeTerm.times(velTerm);

		Polynomial linearPoly = Polynomial.valueOf(Real.ONE, velTimeTerm);

		Polynomial constPoly = Polynomial.valueOf(Real.ONE, rangePosStartVar);

		Polynomial constPlusLinear = constPoly.plus(linearPoly);

		Polynomial fullPoly = constPlusLinear.plus(parabPoly);

		return fullPoly;
	}
	public static NumberFactory<Real> getRealNumberFactory() {
		return new NumberFactory<Real> () {
			@Override public Real getZero() {
				return Real.ZERO;
			}
			@Override public Real getOne() {
				return Real.ONE;
			}
			@Override public Real getOneHalf() {
				throw new RuntimeException("need to replace code for Real.valueOf() for compat with latest Javolution");
				// return Real.valueOf(0.5);
			}
			@Override public Real makeNumberFromDouble(double d) {
				return makeReal(d);
			}

			@Override public Real[] makeArray(int size) {
				return makeArrayForClass(Real.class, size);
			}

		};
	}
}
