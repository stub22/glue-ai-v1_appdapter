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
import org.appdapter.core.math.set.EuclideanSet;
import org.appdapter.core.math.set.RealSet;
import org.appdapter.core.math.set.TopologicalSet;
import org.jscience.mathematics.number.Float64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class Intervals {
	static Logger theLogger = LoggerFactory.getLogger(Intervals.class);
	Reals.Real64 rrr = new Reals.Real64(Float64.ONE);
	RealNumeric<? super Reals.Real64> r2 = rrr;

	public static abstract class RealSet64 extends RealSet.Basic<Reals.Real64, Reals.NonnegativeReal64, Integers.Natural64> {

		public RealSet64(Natural64 dim) {
			super(dim);
		}

	}
	//public static class RealVectorSet64 extends EuclideanSet.Basic<Reals.Real64, Reals.NonnegativeReal64, Integers.Natural64> {
	//}
	
	public static void main(String args[]) {
		theLogger.info("Hi!");
	}
}
