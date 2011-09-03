/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.appdapter.bind.math.jscience.number;

import java.lang.reflect.Array;

/**
 *
 * @author winston
 */
public class GeneralFactory {
	public static <AnyType> AnyType[] makeArrayForClass(Class<AnyType>  type, int size) {
		return (AnyType[])Array.newInstance(type,size);
	}
}
