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

package org.appdapter.core.math.scala
//import org.appdapter.core.math.calculus.RealInterval;
/**
 * @author Stu B. <www.texpedient.com>
 */

object Functor {
	trait ConcreteInterval {}
	/*
	case class ConcreteInterval[V,MDRN,DimN](val myLowBound : V, val myLowBoundOpen: Boolean, 
								val myHighBound: V, val myHighBoundOpen : Boolean, dim: DimN) 
			extends RealInterval.Basic[V,MDRN,DimN](dim){

		override def getLowBound() : V = myLowBound;
		override def getHighBound() : V = myHighBound;
		override def isLowBoundOpen() : Boolean = myLowBoundOpen;
		override def isLowBoundClosed() : Boolean = !myLowBoundOpen;
		
		override def isHighBoundOpen() : Boolean = myHighBoundOpen;
		override def isHighBoundClosed() : Boolean = !myHighBoundOpen;
	}
	*/

}
