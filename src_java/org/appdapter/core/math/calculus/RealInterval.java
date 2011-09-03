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
package org.appdapter.core.math.calculus;

import org.appdapter.core.math.number.IntegNumeric;
import org.appdapter.core.math.number.RealNumeric;
import org.appdapter.core.math.set.RealSet;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface RealInterval<RN extends RealNumeric<RN>,
				MDRN extends RealNumeric.Nonnegative<MDRN>, 
				DimN extends IntegNumeric.Natural<DimN>> 
		extends RealSet<RN, MDRN, DimN> {
	
	public RN getLowerBound();
	public RN getUpperBound();
	public boolean isLowerBoundOpen();
	public boolean isLowerBoundClosed();
	public boolean isUpperBoundOpen();
	public boolean isUpperBoundClosed();
	
	
	public abstract class Basic<RN extends RealNumeric<RN>, 
				MDRN extends RealNumeric.Nonnegative<MDRN>, 
				DimN extends IntegNumeric.Natural<DimN>> 
			extends RealSet.Basic<RN, MDRN, DimN> implements RealInterval<RN, MDRN, DimN> {
		public Basic(DimN dim) {
			super(dim);
		}
		public boolean isLessThan(RN a, RN b) {
			//  compareTo
			//  "Returns a negative integer, zero, or a positive integer as this object is less than, 
			//  equal to, or greater than the specified object."
			// 0 !==>   ".equals()", but usually should
			return a.compareTo(b) < 0;
		}
		public boolean isBelowLowerBound(RN v) {
			return isLessThan(v, getLowerBound());
		}
		public boolean isAboveUpperBound(RN v) {
			return isLessThan(getUpperBound(), v);	
		}
		@Override public boolean containsElement(RN v) {
			return (   (!isBelowLowerBound(v)) && (!isAboveUpperBound(v))   );	
		}
	}

	public abstract class Maker<RN extends RealNumeric<RN>,
				MDRN extends RealNumeric.Nonnegative<MDRN>, 
				DimN extends IntegNumeric.Natural<DimN>> {
		public abstract RealInterval<RN, MDRN, DimN> makeInterval(RN lowBound, boolean lowBoundOpen, RN highBound, boolean highBoundOpen);
		public RealInterval<RN, MDRN, DimN> makeClosedInterval(RN lowBound, RN highBound) {
			return makeInterval(lowBound, false, highBound, false);
		}
		public RealInterval<RN, MDRN, DimN> makeOpenInterval(RN lowBound, RN highBound) {
			return makeInterval(lowBound, true, highBound, true);
		}
	}

}
