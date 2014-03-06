package org.appdapter.bind.math.jscience.number;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import org.jscience.mathematics.number.FieldNumber;
/**
 *
 * @author winston
 */
public interface WackyNumberFactory <FNT extends FieldNumber<FNT>> extends RingElementFactory<FNT> {
	public abstract FNT getOneHalf();
	public abstract FNT makeFromDouble(double d);
	public abstract FNT makeFromInt(int i);
}
