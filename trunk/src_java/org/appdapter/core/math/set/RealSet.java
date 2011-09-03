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
public interface RealSet<
			RN extends RealNumeric<RN>,  
			MDRN extends RealNumeric.Nonnegative<MDRN>, 
			DimN extends IntegNumeric.Natural<DimN> > 
		extends EuclideanSet<RN, MDRN, MDRN, DimN> { // , PartiallyOrderedSet<RN> {
	
	public abstract class Basic<RN extends RealNumeric<RN>, 
					MDRN extends RealNumeric.Nonnegative<MDRN>, 
					DimN extends IntegNumeric.Natural<DimN>> 
			extends EuclideanSet.Basic<RN, MDRN, DimN> 	implements RealSet<RN, MDRN, DimN> {	
		public Basic(DimN dim) {
			// assert dim=="one"
			super(dim);
		}
	}
}
