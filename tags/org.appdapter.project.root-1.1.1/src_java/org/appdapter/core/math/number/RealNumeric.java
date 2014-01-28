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
public interface RealNumeric<RN> extends ExtendedRealNumeric<RN>, Numeric.Finite<RN> {
	
	// RealNumeric.Nonnegative is suitable for measures and norms on ordinary Euclidean spaces.
	public interface Nonnegative<NNRN> extends RealNumeric<NNRN>, ExtendedRealNumeric.Nonnegative<NNRN> { }
	
	public interface Positive<PRN> extends Nonnegative<PRN>, ExtendedRealNumeric.Positive<PRN> { }

}