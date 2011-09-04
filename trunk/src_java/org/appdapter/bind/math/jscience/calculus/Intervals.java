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


import org.appdapter.bind.math.jscience.calculus.Integers.Natural64;
import org.appdapter.bind.math.jscience.calculus.Reals.NonnegativeReal64;
import org.appdapter.bind.math.jscience.calculus.Reals.Real64;
import org.appdapter.core.math.number.RealNumeric;
import org.appdapter.core.math.set.RealSet;
import org.appdapter.core.math.set.TopologicalSet;
import org.jscience.mathematics.number.Float64;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class Intervals {
	Reals.Real64 rrr = new Reals.Real64(Float64.ONE);
	
	RealNumeric<? super Reals.Real64> r2 = rrr;
	public static class Realty extends RealSet.Basic<Reals.Real64, Reals.NonnegativeReal64, Integers.Natural64> {

		public Realty(Natural64 dim) {
			super(dim);
		}

		@Override
		public NonnegativeReal64 distanceForElementPair(Real64 a, Real64 b) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isComplete() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isBounded() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isTotallyBounded() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isOpen() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isClosed() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public TopologicalSet<Real64> getFrontier() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public TopologicalSet<Real64> getClosure() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean containsElement(Real64 v) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public NonnegativeReal64 measure() {
			throw new UnsupportedOperationException("Not supported yet.");
		}
		
	}
}
