/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package	org.appdapter.peru.core.process;

import org.appdapter.peru.core.config.Config;
import org.appdapter.peru.core.name.Address;
import org.appdapter.peru.core.environment.Environment;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
 
public interface Processor {
	
/**
  *  Perform command, transforming input into output, using/modifying the world as needed (but as little as possible!).
  *  May have side effects which modify internal state of the Processor.  Not necessarily idempotent.
  *
  *  Semantics of the instruction should be defined in appropriate models.
  *
  *  An abstract syntax for describing execution is:
  * 
  * {o|t,l} = p(x,c,i,w)   where x and w represent the "indoor" and "outdoor" state.
  * [c is command <-> instruction]
  *
  * When outdoor state (the world, as accessed through an environment) is factored out, the
  * processing becomes indoor-safe ("housebroken").  When indoor is also factored out, the
  * processing is effectively stateless (hence idempotent).
  *
  * Input and output data must be serializable, so this method may be exposed as a network
  * service, given appropriate wrappers to account for initialization of the processor itself,
  * and establishment of the Environment.
  *
  * Over time, we hope to classify some operations as environment-independent, and offer a
  * narrowed (indoor-only) interface for their processing.
  *
  */
	
	public Data process(Address instruction, Data input, Environment world) throws Throwable;
	
	public void create(Environment world, Data optionalExtraData) throws Throwable;
	
	public void destroy(Environment world) throws Throwable;
	
	public void reconfigure(Config c, Environment world, Address nominalRootAddr) throws Throwable;
	
	public Data getStatusData(Environment world) throws Throwable;
	
	
}