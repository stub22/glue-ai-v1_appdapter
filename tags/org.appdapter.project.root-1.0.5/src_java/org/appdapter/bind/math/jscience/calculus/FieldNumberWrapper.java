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
import org.appdapter.core.math.number.Numeric;	
import org.jscience.mathematics.number.FieldNumber;

/**
 * @author Stu B. <www.texpedient.com>
 */
abstract class FieldNumberWrapper<
				FNW extends FieldNumberWrapper<FNW, JSFN>, 
				JSFN extends FieldNumber<JSFN>> 
		extends FieldNumber<FieldNumberWrapper<FNW, JSFN>> 
		implements Numeric<FieldNumberWrapper<FNW, JSFN>> {

	private JSFN myJScienceNumber;

	protected JSFN getJScienceNumber() {
		return myJScienceNumber;
	}

	protected RuntimeException downcastFailureException(Class targetType) {
		return new RuntimeException("Cannot treat [" + this + "] as " + targetType);
	}

	public FieldNumberWrapper(JSFN jsciNum) {
		myJScienceNumber = jsciNum;
	}
	@Override public long longValue() {
		return myJScienceNumber.longValue();
	}

	@Override public double doubleValue() {
		return myJScienceNumber.doubleValue();
	}

	@Override public BigDecimal decimalValue() {
		return myJScienceNumber.decimalValue();
	}

	@Override public int compareTo(FieldNumberWrapper<FNW, JSFN> otherFN) {
		return myJScienceNumber.compareTo(otherFN.getJScienceNumber());
	}
}
