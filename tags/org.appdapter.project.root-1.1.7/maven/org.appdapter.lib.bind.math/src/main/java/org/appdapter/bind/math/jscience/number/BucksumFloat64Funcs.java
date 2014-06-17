/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.appdapter.bind.math.jscience.number;

import org.jscience.mathematics.number.Float64;


/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class BucksumFloat64Funcs {
	public static Float64 makeFloat64(double doubleValue) {
		return Float64.valueOf(doubleValue);
	}
	public static FieldNumberFactory<Float64> getNumberFactory() {
		return new FieldNumberFactory<Float64> () {
			@Override public Float64 getZero() {
				return Float64.ZERO;
			}
			@Override public Float64 getOne() {
				return Float64.ONE;
			}
			@Override public Float64 makeNumberFromDouble(double d) {
				return makeFloat64(d);
			}
			@Override public Float64[] makeArray(int size) {
				return GeneralFactory.makeArrayForClass(Float64.class, size);
			}
			public Float64 makeNumberFromInt(int i) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}
}
