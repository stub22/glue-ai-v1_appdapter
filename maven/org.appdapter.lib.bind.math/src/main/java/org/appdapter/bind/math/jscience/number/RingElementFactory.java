package org.appdapter.bind.math.jscience.number;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import org.jscience.mathematics.structure.Ring;

/**
 *
 * @author winston
 */
public interface RingElementFactory<R extends Ring<R>> {
	public abstract R getZero();
	public abstract R getOne();
	public abstract R[] makeArray(int size);
}
