/*
 *  Copyright 2014 by The Friendularity Project (www.friendularity.org).
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

package org.appdapter.fancy.log

import org.appdapter.core.log.BasicDebugger;

trait VarargsLogging extends HasLoggerConv { }

/*
 *
 *  http://stackoverflow.com/questions/6051302/what-does-colon-underscore-star-do-in-scala
 * 		//  https://groups.google.com/forum/#!topic/scala-language/ms4IVIu-xGw
 /*
  * in slf4j there are several overloads for the logging methods [1]: 
 
  void        info(String format, Object... arguments) 
  void        info(String format, Object arg) 
  void        info(String format, Object arg1, Object arg2) 

  When trying to call this method, the Scala compiler fails with 
  "ambiguous reference to overloaded definition" as shown in ticket 
  SI-4728 [2]. The problem is that there seems to be no workaround which 
  allows to disambiguate which overload to call. You can use the var-arg 
  method (by using `Seq(...): _*` at the call site) but not the 
  optimized non-vararg variants. 
  */
 
 // To pass an explicit sequence to a method that takes varargs, follow it with : _*
		
 http://stackoverflow.com/questions/6051302/what-does-colon-underscore-star-do-in-scala
 It "splats"1 the sequence.
 The _* type annotation is covered in "4.6.2 Repeated Parameters" of the SLS.

 The last value parameter of a parameter section may be sufïxed by, e.g. (..., x:T *). 
 The type of such a repeated parameter inside the method is then the sequence type scala.Seq[T]. 
 Methods with repeated parameters T * take a variable number of arguments of type T . That is, if a method m with 
 type (p1 : T1, . . . , pn : Tn,ps : S*)U is applied to arguments (e1, . . . , ek) where k >= n, then m is taken 
 in that application to have type (p1 : T1, . . . , pn : Tn,ps : S, . . . , ps0S)U, with  occurrences of type 
 S where any parameter names beyond ps are fresh. The only exception to this rule is if the last argument is marked 
 to be a sequence argument via a _* type annotation. If m above is applied to arguments (e1, . . . , en,e0 : _*), 
 then the type of m in that application is taken to be (p1 : T1, . . . , pn : Tn,ps :scala.Seq[S])
 */
