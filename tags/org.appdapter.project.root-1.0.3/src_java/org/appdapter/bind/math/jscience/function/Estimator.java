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

import org.appdapter.core.math.calculus.EstimableScalarFunction;
import org.appdapter.core.math.calculus.RealInterval;

import org.jscience.mathematics.structure.Ring;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class Estimator <DomainValue, RangeNumber extends Ring<RangeNumber>, PositiveRangeNumber extends RangeNumber>  {
	/*
	public RealInterval<RangeNumber> makeEstimateInterval(EstimableScalarFunction<DomainValue, 
				RangeNumber, PositiveRangeNumber> func, DomainValue domainVal, PositiveRangeNumber maxAbsError,
				RealInterval.Maker<RangeNumber> maker) {
		
		RangeNumber estimate = func.estimateValue(domainVal, maxAbsError);
		RangeNumber upperBound = estimate.plus(maxAbsError);
		RangeNumber lowerBound = estimate.plus(maxAbsError.opposite());
		return maker.makeInterval(lowerBound, false, lowerBound, false);
	}
	public RealInterval<RangeNumber> makeDefiniteIntegralEstimateInterval(LebesgueIntegrableFunction<DomainValue, 
				RangeNumber, PositiveRangeNumber> func, RealInterval<DomainValue> domainBounds, PositiveRangeNumber maxAbsError,
				RealInterval.Maker<RangeNumber> maker) {
		return makeEtimateInterval(func, domainBounds, maxAbsError, maker);
		
	}
*/
}
