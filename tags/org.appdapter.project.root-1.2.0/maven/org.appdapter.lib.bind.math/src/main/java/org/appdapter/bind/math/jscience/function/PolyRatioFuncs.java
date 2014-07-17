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


import org.appdapter.bind.math.jscience.number.FieldNumberFactory;
import org.appdapter.bind.math.jscience.number.RingElementFactory;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.RationalFunction;
import org.jscience.mathematics.number.FieldNumber;
import org.jscience.mathematics.structure.Field;

/**
 *
 * @author winston
 */
public class PolyRatioFuncs {
	public static <F extends Field<F>> RationalFunction<F> makeConstantPolyRatio(F constant, RingElementFactory<F> ref) {
		Polynomial<F>	numer = PolyFuncs.makeConstantPoly(constant);
		Polynomial<F>	denom = PolyFuncs.makeUnitPoly(ref);
		return RationalFunction.valueOf(numer, denom);
	}
	public static <F extends Field<F>> RationalFunction<F> makeUnitPolyRatio(RingElementFactory<F> ref) {
		return makeConstantPolyRatio(ref.getOne(), ref);
	}
	public static <F extends Field<F>> RationalFunction<F> makeZeroPolyRatio(RingElementFactory<F> ref) {
		return makeConstantPolyRatio(ref.getZero(), ref);
	}
	public static <FN extends FieldNumber<FN>> RationalFunction<FN> makeConstantPolyRatio(FieldNumberFactory<FN> dnf, double constant) {
		FN cn = dnf.makeNumberFromDouble(constant);
		return makeConstantPolyRatio(cn, dnf);
	}
}
