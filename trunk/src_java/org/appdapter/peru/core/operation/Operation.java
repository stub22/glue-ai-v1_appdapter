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

package org.appdapter.peru.core.operation;

import java.util.Map;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

import com.hp.hpl.jena.rdf.model.Model;

/** 
 * <br/>An Operation is a standalone implementation of a piece of application functionality.  
 * <br/>An operation has a single execute method, which may be called multiple times, and no pre-defined 
 * "setup" or "teardown" methods.  A particular implementation class may have its own setup/teardown logic,
 * factory paradigm, etc., but this structure is decoupled from the peruser substrate infrastructure.
 * <br/>All instances of an Operation class must be semantically equivalent.
 * <br/>All information context for Operation processing must be supplied in the arguments to the execute method.
 * <br/>A particular Operation class may provide for some technology resource state (e.g. a database connection),
 * but no application information state.
 * <br/>
 * The Operation pattern is conceptually compatible with the EJB stateless session bean pattern.
 * <br/>
 * Operation stands in contrast to Machine, which is a re-usable, stateful document processor pattern, and
 * Command, which is a non-repeatable stateful semantic transaction pattern.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public interface Operation {
	
/** 
 * @param op     A read-only individual from input who describes/defines/instructs this operation.
 * @param input  A read-only Jena ontology model, which contains op and other read-only config for us.
 * @param target A read/write Jena model, which must already exist if/when it is accessed from execute().
 * @param params A read-only java parameter map, i.e. a set of (key, value) pairs of Java objects.  Use sparingly!
 * is configured by a Jena ont-model and a Java map of params, and may write into an output "target" Jena model.
 * @return       A reference to ANY model, which may be newly created, == one of the input models, or any other.
 *
 */
    public Model execute(Individual op, OntModel input, Model target, Map params) throws Throwable;
}

