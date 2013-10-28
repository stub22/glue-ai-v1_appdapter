/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.appdapter.bind.math.jscience.number;

import org.jscience.mathematics.number.Float64;


/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class Float64Funcs {
	public static Float64 makeFloat64(double doubleValue) {
		return Float64.valueOf(doubleValue);
	}
	public static NumberFactory<Float64> getNumberFactory() {
		return new NumberFactory<Float64> () {
			@Override public Float64 getZero() {
				return Float64.ZERO;
			}
			@Override public Float64 getOne() {
				return Float64.ONE;
			}
			@Override public Float64 getOneHalf() {
				return Float64.valueOf(0.5);
			}
			@Override public Float64 makeNumberFromDouble(double d) {
				return makeFloat64(d);
			}
			public Float64 makeNumberFromInt(int i) {
				throw new UnsupportedOperationException("Not supported yet.");
			}			
			@Override public Float64[] makeArray(int size) {
				return makeArrayForClass(Float64.class, size);
			}
		};
	}
}
