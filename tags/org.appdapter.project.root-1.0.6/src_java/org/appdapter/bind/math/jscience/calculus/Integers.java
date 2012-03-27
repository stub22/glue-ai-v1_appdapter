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

import java.math.BigDecimal;
import org.appdapter.core.math.number.IntegNumeric;
import org.appdapter.core.math.number.IntegNumeric.Natural;
import org.appdapter.core.math.number.IntegNumeric.Whole;
import org.appdapter.core.math.number.IntegNumeric.Zero;
import org.appdapter.core.math.number.Numeric;
import org.jscience.mathematics.number.Integer64;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class Integers {
	
	public static class Int64 extends NumberWrapper<Int64, Integer64> 
			implements IntegNumeric<NumberWrapper<Int64, Integer64>> {

		public Int64(Integer64 jsciNum) {
			super(jsciNum);
		}

		@Override public boolean isScalar() {
			return true;
		}

		@Override public boolean isZero() {
			return getJScienceNumber().equals(Integer64.ZERO);
		}

		@Override public boolean isPositive() {
			return getJScienceNumber().isGreaterThan(Integer64.ZERO);
		}

		@Override public boolean isNonnegative() {
			return getJScienceNumber().isLessThan(Integer64.ZERO);
		}

		@Override public boolean isFinite() {
			return true;
		}

		@Override public Whole64 asNonnegative() {
			if(this instanceof Whole64) {
				return (Whole64) this;
			} else {
				if (isNonnegative()) { 
					return new Whole64(getJScienceNumber());
				} else {
					throw downcastFailureException(Whole64.class);
				}
			}
		}

		@Override public Natural64 asPositive() {
			if(this instanceof Natural64) {
				return (Natural64) this;
			} else {
				if (isPositive()) { 
					return new Natural64(getJScienceNumber());
				} else {
					throw downcastFailureException(Natural64.class);
				}
			}
		}

		@Override public Zero64 asZero() {
			if(this instanceof Zero64) {
				return (Zero64) this;
			} else {
				if (isZero()) { 
					return new Zero64(getJScienceNumber());
				} else {
					throw downcastFailureException(Zero64.class);
				}
			}
		}
		@Override public Int64 asFinite() {
			return this;
		}
		// Here is the point:  I know that abs() returns Whole64 (because number
		// is positive).  
		@Override public Whole64 abs() {
			if (isNonnegative()) {
				return asNonnegative();
			} else {
				return new Whole64(getJScienceNumber().abs());
			}
		}

		@Override public Int64 copy() {
			return new Int64(getJScienceNumber());
		}

		@Override public Int64 times(NumberWrapper<Int64, Integer64> other) {
			return new Int64(getJScienceNumber().times(other.getJScienceNumber()));
		}

		@Override public Int64 plus(NumberWrapper<Int64, Integer64> other) {
			return new Int64(getJScienceNumber().plus(other.getJScienceNumber()));
		}

		@Override public Int64 opposite() {
			return new Int64(getJScienceNumber().opposite());
		}
				
	}
	public static class Whole64 extends Int64 implements Whole<NumberWrapper<Int64, Integer64>> {
		public Whole64(Integer64 jsciNum) {
			super(jsciNum);
			if (!isNonnegative()) {
				downcastFailureException(Whole64.class);
			}			
		}
	}

	public static class Natural64 extends Whole64 implements Natural<NumberWrapper<Int64, Integer64>> {
		public final static Natural64 ONE = new Natural64(Integer64.ONE);
		public Natural64(Integer64 jsciNum) {
			super(jsciNum);
			if (!isPositive()) {
				downcastFailureException(Natural64.class);
			}
		}
	}
	public static class Zero64 extends Whole64 implements Zero<NumberWrapper<Int64, Integer64>> {
		public final static Zero64 ZERO = new Zero64(Integer64.ZERO);
		public Zero64(Integer64 jsciNum) {
			super(jsciNum);
			if (!isZero()) {
				downcastFailureException(Zero64.class);
			}
		}
	}
		

}
