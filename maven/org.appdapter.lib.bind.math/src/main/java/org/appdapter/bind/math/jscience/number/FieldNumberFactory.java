/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.appdapter.bind.math.jscience.number;

import org.jscience.mathematics.number.FieldNumber;

/**
 *
 * @author winston
 */

public interface FieldNumberFactory <FN extends FieldNumber<FN>> extends RingElementFactory<FN> {
	public abstract FN makeNumberFromDouble(double d);
	public abstract FN makeNumberFromInt(int i);
}

