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
// We cannot relate MDRN to RN in Java
// If we try:   MDRN extends RN & RealNumeric.Nonnegative<? super MDRN>
// we get:  "A type variable may not be followed by other bounds"
// http://blogs.oracle.com/vr/entry/a_type_variable_may_not

// However, should work in Scala.

public interface RealSet<
			RN extends RealNumeric<? super RN>,  
			MDRN extends RealNumeric.Nonnegative<? super MDRN>, 
			DimN extends IntegNumeric.Natural<? super DimN> > 
		extends EuclideanSet<RN, MDRN, MDRN, DimN> { // , PartiallyOrderedSet<RN> {
	
	public abstract class Basic<RN extends RealNumeric<? super RN>, 
					MDRN extends RealNumeric.Nonnegative<? super MDRN>, 
					DimN extends IntegNumeric.Natural<? super DimN>> 
			extends EuclideanSet.Basic<RN, MDRN, DimN> 	implements RealSet<RN, MDRN, DimN> {	
		public Basic(DimN dim) {
			// assert dim=="one"
			super(dim);
		}
	}
}
