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

package org.appdapter.scafun

object OkFine {
  def main(args: Array[String]) :Unit = {
	  	println("OkFine "); //  + portfolioValue.toString());
		println("Mapping stuff to other stuff, blending dynamic approximation and ironic detachment.");
		LuckyTest.test();
		/* Our basis functions produce values using no side effects, and no system access.  We do not read or write
		 * files or sockets or other goodies in any of our basis functions.  Nor do we create any random number
		 * generators, although we may use such an RNG if passed in to us.  The objects queried by our basis
		 * functions should not
				" or socket) access");
When an algebraic structure includes more than one operation, homomorphisms are required to preserve each operation.
For example, a ring possesses both addition and multiplication, and a homomorphism between two rings is a function
such that
	*/


  }
}

/* All methods on a D. object D.O. must be D.   Specifically, they must
 *	1) Produce no side-effects outside D.O.
 *	2) Make no reads or writes to any system resource such as a file, socket, thread, console, classpath entry, or system clock.
 *		a) Access to the "System" and "Runtime" classes of Java is specifically forbidden!
 *		b) Reads/writes to internal queues of the process (e.g. for logging or message passing) is also forbidden
 *			(except when they are entirely within D.O.
 *	3) Avoid creating any random number generators, although generators supplied as inputs may be used deterministically.
 *
 *	It is common but not necessary for D. objects to be serializable.
 */
trait Deterministic {
}

class BasisFunction extends Object with Deterministic {
	
}