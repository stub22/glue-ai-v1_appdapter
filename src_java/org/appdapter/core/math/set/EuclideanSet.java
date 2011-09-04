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
package org.appdapter.core.math.set;

import org.appdapter.core.math.number.IntegNumeric;
import org.appdapter.core.math.number.RealNumeric;


/**
 * @author Stu B. <www.texpedient.com>
 */

// DistN and MeasN are both "real numbers", as are the coordinates of V, but each may
// use a different concrete type.
public interface EuclideanSet<V, 
					DistN extends RealNumeric.Nonnegative<? super DistN>, 
					MeasN extends RealNumeric.Nonnegative<? super MeasN>,
					DimN extends IntegNumeric.Natural<? super DimN>> 

			extends MetricSet<V, DistN>, MeasurableSet<V, MeasN>, DimensionalSet<V,DimN> {
	
	@Override public DimN getDimension();
	
	public abstract class Basic<V, 
					MeasAndDistRN extends RealNumeric.Nonnegative<? super MeasAndDistRN>,
					DimN extends IntegNumeric.Natural<? super DimN>> 
			extends MetricSet.Basic<V, MeasAndDistRN> 	
					implements EuclideanSet<V, MeasAndDistRN, MeasAndDistRN, DimN> {
		
		private		DimN		myDimension;
		public Basic(DimN dim) {
			myDimension = dim;
		}
		@Override public DimN getDimension() {
			return myDimension;
		}
	}

}
