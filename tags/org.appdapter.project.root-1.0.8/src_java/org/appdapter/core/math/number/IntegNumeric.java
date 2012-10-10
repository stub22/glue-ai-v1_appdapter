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
public interface IntegNumeric<IN> extends SeqNumeric.Finite<IN> {
	
	// public interface Nonnegative<NNIN> extends SeqNumeric.Nonnegative<NNIN> {}
	public interface Whole<WIN> extends IntegNumeric<WIN>, SeqNumeric.Nonnegative<WIN> { }

	public interface Natural<NIN> extends Whole<NIN>, SeqNumeric.Positive<NIN>, Cardinal<NIN>, Ordinal<NIN>  { }	

	public interface Zero<ZIN> extends Whole<ZIN>, Numeric.Zero<ZIN> { }
	
	
	
}
