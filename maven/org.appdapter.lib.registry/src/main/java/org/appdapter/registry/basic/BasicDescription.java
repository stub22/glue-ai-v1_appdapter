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
package org.appdapter.registry.basic;

import org.appdapter.api.registry.Description;

/**
 * @author Stu B. <www.texpedient.com>
 * This class is final to prevent identity/equality mishaps.
 */
public final class BasicDescription implements Description {
	private final String myName;
	public BasicDescription(String name) {
		myName = name;
	}
	@Override public String getName() {
		return myName;
	}
	@Override public int hashCode() { 
		return myName.hashCode();
	}
	@Override public boolean equals(Object other) { 
		if ((other != null) && (myName != null)) {
			if (other instanceof BasicDescription) {
				BasicDescription obd = (BasicDescription) other;
				return myName.equals(obd.getName());
			}
		}
		return false;
	}
}
