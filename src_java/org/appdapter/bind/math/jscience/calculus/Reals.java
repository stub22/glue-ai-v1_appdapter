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
package org.appdapter.bind.math.jscience.calculus;

import org.appdapter.core.math.number.RealNumeric;
import org.jscience.mathematics.number.FieldNumber;
import org.jscience.mathematics.number.Float64;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class Reals {
	public static class Real64 extends FieldNumberWrapper<Real64, Float64>
			// IF FieldNumberWrapper is a Numeric.
			// If we do RealNumeric<Real64>, we get complaint about Numeric<> inherited twice with diff parameters.
			// implements RealNumeric<AnyFieldNumber<Real64, Float64>> {
			// IF FieldNumberWrapper is not a Numeric, we get "java.lang.Comparable" cannot be inherited with diff params.
			// If we make everything a <AnyFieldNumber<Real64, Float64>>, then on use we get
			// complaints that for RealNumeric<Real64>, RN is not within its type bound.
			// implements RealNumeric<Real64> {
			implements RealNumeric<FieldNumberWrapper<Real64, Float64>> {

		public Real64(Float64 jsciNum) {
			super(jsciNum);
		}
		
		@Override public FieldNumberWrapper<Real64, Float64> abs() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public FieldNumberWrapper<Real64, Float64> copy() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public FieldNumberWrapper<Real64, Float64> times(FieldNumberWrapper<Real64, Float64> r) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public FieldNumberWrapper<Real64, Float64> plus(FieldNumberWrapper<Real64, Float64> g) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public FieldNumberWrapper<Real64, Float64> opposite() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public FieldNumberWrapper<Real64, Float64> inverse() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public boolean isScalar() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public boolean isZero() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public boolean isPositive() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public boolean isNonnegative() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public boolean isFinite() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public NonnegativeReal64 asNonnegative() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public PositiveReal64 asPositive() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public Zero<FieldNumberWrapper<Real64, Float64>> asZero() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public Finite<FieldNumberWrapper<Real64, Float64>> asFinite() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override public boolean isRealNumeric() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override public RealNumeric<AnyFieldNumber<Real64, Float64>> asRealNumeric() {
		@Override public Real64 asRealNumeric() {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}
	public static class NonnegativeReal64 extends Real64 
			implements RealNumeric.Nonnegative<FieldNumberWrapper<Real64, Float64>> {

		public NonnegativeReal64(Float64 jsciNum) {
			super(jsciNum);
			if (!isNonnegative()) {
				downcastFailureException(NonnegativeReal64.class);
			}
		}
	}
	public static class PositiveReal64 extends NonnegativeReal64
			implements RealNumeric.Positive<FieldNumberWrapper<Real64, Float64>> {

		public PositiveReal64(Float64 jsciNum) {
			super(jsciNum);
			if (!isPositive()) {
				downcastFailureException(PositiveReal64.class);
			}
		}
	}	
}
