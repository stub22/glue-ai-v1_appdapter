/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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
/**
 * 
 * <p>
 * Micro-framework of defined execution lifecycles for parallel state machines.
 * After init+start, Module machines expect their runOnce methods to be called
 * as often as practically possible (given machine resources and priorities).
 * Modules endeavor to make "runOnce" perform the smallest possible consistent 
 * chunk of work.  The logic for start+stop of a Module is completely orthogonal
 * to its runOnce business payload.  
 * </p>
 * <p>
 * Modules are generally run by a Modulator, which typically will own a single 
 * JVM work thread that it uses for all callbacks into its Modules.   The Modulator
 * provides an API for clients to start and stop modules.  A particular Modulator
 * may employ any kind of fairness and priority policies.
 * </p>
 * <p>
 * Module has a pointer to a single parent Modulator of generic type.
 * Thus, Modules are parametrized by the types of Modulators they require.
 * It is possible to construct a pattern where a Module's callbacks obtain
 * important resources by request from its parent-Mu, which may implement useful
 * synchronization policies for a collection of modules following the pattern.
 * </p>
 */


package org.appdapter.api.module;