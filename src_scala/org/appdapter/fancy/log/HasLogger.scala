/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// A simple trait to add SLF4j logging
trait HasLogger {
  val myLogger : Logger = LoggerFactory.getLogger(this.getClass);
  def getLogger() : Logger = myLogger
}

trait HasLoggerConv extends HasLogger {
	def info0(msg : String) = 	getLogger().info(msg)
	def info1(msg : String, v1 : Object) = 	getLogger().info(msg, v1)
	def info2(msg : String, v1 : Object, v2: Object) = 	getLogger().info(msg, Seq(v1, v2) : _*)
	def info3(msg : String, v1 : Object, v2: Object, v3: Object) = 	getLogger().info(msg, Seq(v1, v2, v3) : _*)
	def info4(msg : String, v1 : Object, v2: Object, v3: Object, v4: Object) = 	getLogger().info(msg, Seq(v1, v2, v3 ,v4) : _*)	
	
	def debug0(msg : String) = 	getLogger().debug(msg)
	def debug1(msg : String, v1 : Object) = 	getLogger().debug(msg, v1)
	def debug2(msg : String, v1 : Object, v2: Object) = 	getLogger().debug(msg, Seq(v1, v2) : _*)
	def debug3(msg : String, v1 : Object, v2: Object, v3: Object) = 	getLogger().debug(msg, Seq(v1, v2, v3) : _*)
	def debug4(msg : String, v1 : Object, v2: Object, v3: Object, v4: Object) = 	getLogger().debug(msg, Seq(v1, v2, v3 ,v4) : _*)	
	
	def warn0(msg : String) = 	getLogger().warn(msg)
	def warn1(msg : String, v1 : Object) = 	getLogger().warn(msg, v1)
	def warn2(msg : String, v1 : Object, v2: Object) = 	getLogger().warn(msg, Seq(v1, v2) : _*)
	def warn3(msg : String, v1 : Object, v2: Object, v3: Object) = 	getLogger().warn(msg, Seq(v1, v2, v3) : _*)	
	def warn4(msg : String, v1 : Object, v2: Object, v3: Object, v4: Object) = 	getLogger().warn(msg, Seq(v1, v2, v3 ,v4) : _*)	
	
	def trace0(msg : String) = 	getLogger().trace(msg)
	def trace1(msg : String, v1 : Object) = 	getLogger().trace(msg, v1)
	def trace2(msg : String, v1 : Object, v2: Object) = 	getLogger().trace(msg, Seq(v1, v2) : _*)
	def trace3(msg : String, v1 : Object, v2: Object, v3: Object) = 	getLogger().trace(msg, Seq(v1, v2, v3) : _*)	
	def trace4(msg : String, v1 : Object, v2: Object, v3: Object, v4: Object) = 	getLogger().trace(msg, Seq(v1, v2, v3 ,v4) : _*)
	
	def error0(msg : String) = 	getLogger().error(msg)
	def error1(msg : String, v1 : Object) = 	getLogger().error(msg, v1)
	def error2(msg : String, v1 : Object, v2: Object) = 	getLogger().error(msg, Seq(v1, v2) : _*)
	def error3(msg : String, v1 : Object, v2: Object, v3: Object) = 	getLogger().error(msg, Seq(v1, v2, v3) : _*)	
	def error4(msg : String, v1 : Object, v2: Object, v3: Object, v4: Object) = 	getLogger().error(msg, Seq(v1, v2, v3 ,v4) : _*)
	
}