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

package org.appdapter.bind.math.jscience.scala

import org.jscience.mathematics.number.{FieldNumber, Float64};
import org.jscience.mathematics.function.{Polynomial, RationalFunction, Term} ;
import org.jscience.mathematics.structure.{Field};
//import org.appdapter.core.math.scala.Functor.{ConcreteInterval};
// import org.appdapter.core.math.calculus.{Interval};

import org.appdapter.bind.math.jscience.function.{PolyRatioFuncs};
import org.appdapter.bind.math.jscience.probability.{ProbabilityDensityFunction};
import org.appdapter.bind.math.jscience.number.{FieldNumberFactory};
/**
 * @author Stu B. <www.texpedient.com>
 */

object Binder {
	/*
	class FieldNumberInterval[FN <: FieldNumber[FN]](lowBound : FN, lowBoundOpen: Boolean, highBound: FN, highBoundOpen : Boolean) 
			extends ConcreteInterval[FN](lowBound, lowBoundOpen, highBound, highBoundOpen, null)  {
		// FieldNumber provides "divide(Long)" - is this based on a theorem about isomorphism of quotient structures? 		
		def getMidpoint() : FN = lowBound.plus(highBound).divide(2L);
	}
	class FieldNumberThingMaker[FN <: FieldNumber[FN]](myAddZero: FN, myMultOne: FN) extends Interval.Maker[FN] {
		override def makeInterval(lb : FN, lbof : Boolean, hb : FN, hbof : Boolean) = new FieldNumberInterval[FN](lb, lbof, hb, hbof);
		def  makeSymmetricIntervalForHalfWidth(midpoint : FN, halfWidth : FN, openFlag: Boolean) : FieldNumberInterval[FN] = {
			makeInterval(midpoint.minus(halfWidth), openFlag, midpoint.plus(halfWidth), openFlag);
		}
		def  makeSymmetricIntervalForFullWidth(midpoint : FN, fullWidth : FN, openFlag: Boolean) : FieldNumberInterval[FN] = {
			// FieldNumber provides "divide(Long)" - is this based on a theorem about isomorphism of quotient structures? 
			makeSymmetricIntervalForHalfWidth(midpoint, fullWidth.divide(2L), openFlag);
		}
	}
	*/

}
