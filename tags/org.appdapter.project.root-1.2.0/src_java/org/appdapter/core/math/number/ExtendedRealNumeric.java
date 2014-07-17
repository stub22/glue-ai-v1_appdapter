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
package org.appdapter.core.math.number;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface ExtendedRealNumeric <ERN> extends Numeric.Scalar<ERN>, Numeric.Comparable<ERN> {
	
	public boolean isRealNumeric();
	public RealNumeric<ERN> asRealNumeric(); 
	
	// ExtendedRealNumeric.Nonnegative is eligible to be a Measure.
	public interface Nonnegative<NNERN> extends ExtendedRealNumeric<NNERN>, Numeric.Nonnegative<NNERN> { }
	
	// The closest thing to a colloquial "positive number"
	public interface Positive<PERN> extends Nonnegative<PERN>, Numeric.Positive<PERN> { }
	
	// == MeasureZero

	public interface Infinite<IERN> extends ExtendedRealNumeric<IERN>, Numeric.Infinite<IERN> {
	}

	
	public interface Zero<ZERN> extends Nonnegative<ZERN>, Numeric.Zero<ZERN> {	}
	
}
