/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.appdapter.bind.math.jscience.number;

import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Term;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Real;
import org.slf4j.Logger;

import java.util.List;
import java.util.logging.Level;

/**
 * @author Stu B. <www.texpedient.com>
 *
 *         Currently BROKEN - incompatible with JScience 5.0.
 */
public class BucksumRealFuncs {

	public static Real makeReal(double d) {
		return null;
		/*
		// Real.valueOf seems to have a bug when dealing with negative inputs!
		double mag = Math.abs(d);
		Real magReal  = Real.valueOf(mag); // Not present in JScience 5.0
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
			throw new RuntimeException("Null value for var with sym=" + var.getSymbol());
		}
		return rval.doubleValue();
	}

	public static void setInputVarValue(Polynomial poly, String fullSymbol, double value) {
		Variable<Real> v = poly.getVariable(fullSymbol);
		if (v == null) {
			throw new RuntimeException("Can't locate var for sym=" + fullSymbol + " in poly=" + poly);
		}
		setVariableValue(v, value);
	}

	public static double getInputVarValue(Polynomial poly, String fullSymbol) {
		Variable<Real> v = poly.getVariable(fullSymbol);
		if (v == null) {
			throw new RuntimeException("Can't locate var for sym=" + fullSymbol + " in poly=" + poly);
		}
		Real rval = v.get();
		if (rval == null) {
			throw new RuntimeException("Null value for var with sym=" + fullSymbol + " in poly=" + poly);
		}
		return rval.doubleValue();
	}

	public static double evalPoly(Polynomial<Real> poly) {
		return evalPrintReturnPoly(poly, null, null, null, false);
	}

	public static double evalPrintReturnPoly(Polynomial<Real> poly, String logLabel,
											 Logger logger, Level logLev, boolean shouldLog) {
		Real val = poly.evaluate();
		if (shouldLog) {
			StringBuffer messageBuffer = new StringBuffer(logLabel).append(" = {");
			messageBuffer.append(poly.toString()).append("} (").append(dumpPolyVars(poly)).append(") = ").append(val);

			final String message = messageBuffer.toString();
			if (logLev == Level.SEVERE && logger.isErrorEnabled()) {
				logger.error(message);
			} else if (logLev == Level.WARNING && logger.isWarnEnabled()) {
				logger.warn(message);
			} else if (logLev == Level.INFO && logger.isInfoEnabled()) {
				logger.info(message);
			} else if (logLev == Level.FINE || logLev == Level.FINER && logger.isDebugEnabled()) {
				logger.debug(message);
			} else if (logLev == Level.FINEST && logger.isTraceEnabled()) {
				logger.trace(message);
			}

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

		Term timeSquaredTerm = Term.valueOf(rangeOffsetTimeVar, 2);
		Term accelTerm = Term.valueOf(rangeAccelVar, 1);
		Term accelTimeSqTerm = timeSquaredTerm.times(accelTerm);

		Real oneHalf = BucksumRealFuncs.makeReal(0.5);

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

	public static FieldNumberFactory<Real> getRealNumberFactory() {
		return new FieldNumberFactory<Real>() {
			@Override
			public Real getZero() {
				return Real.ZERO;
			}

			@Override
			public Real getOne() {
				return Real.ONE;
			}

			public Real getOneHalf() {
				return makeNumberFromDouble(0.5);
			}

			@Override
			public Real makeNumberFromDouble(double d) {
				return makeReal(d);
			}

			@Override
			public Real[] makeArray(int size) {
				return GeneralFactory.makeArrayForClass(Real.class, size);
			}

			public Real makeNumberFromInt(int i) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

		};
	}
}
