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
package org.appdapter.module.basic;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class EmptyTimedModule<Ctx> extends TimedModule<Ctx> {

	@Override protected void doInit(Ctx ctx) {
	}

	@Override protected void doStart(Ctx ctx) {
	}

	@Override protected void doRunOnce(Ctx ctx, long runSeqNum) {
	}

	@Override protected void doStop(Ctx ctx) {
	}

	@Override protected void doRelease(Ctx ctx) {
	}
	
}
