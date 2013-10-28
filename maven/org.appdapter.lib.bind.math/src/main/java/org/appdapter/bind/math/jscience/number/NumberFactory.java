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
import java.lang.reflect.Array;

import org.jscience.mathematics.number.Number;

/**
 * @param <RN>
 * @author Stu B. <www.texpedient.com>
 */
public abstract class NumberFactory<RN extends Number<RN>> {
	public abstract RN makeNumberFromDouble(double d);
	public RN makeNumberFromLong(long lnum) {
		// Override this in fancier cases.
		return makeNumberFromDouble(1.0 * lnum);
	}
	public RN makeNumberFromRatioOfLongs(long numer, long denom) {
		// Override this in fancier cases.
		return makeNumberFromDouble(1.0 * numer / denom);
	}

	public RN getZero() {
		return makeNumberFromLong(0);
	}
	public RN getOne() {
		return makeNumberFromLong(1);
	}
	public RN getOneHalf() {
		return makeNumberFromRatioOfLongs(1, 2);
	}
	public abstract RN[] makeArray(int size);

	public static <SRN> SRN[] makeArrayForClass(Class<SRN>  type, int size) {
		return (SRN[])Array.newInstance(type,size);
	}
}
