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

package org.appdapter.peru.core.handle;

import org.appdapter.peru.core.name.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**   A Handle is a runtime reference to a computing resource.
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */

public class Handle {
		
	private static Logger 		theLogger = LoggerFactory.getLogger(Handle.class);	
	
	private 	String		myCuteLocalName;
	private		Address		myPublishedAddress;
	
	public Handle (String cute, Address address) {
		myCuteLocalName = cute;
		myPublishedAddress = address;
	}	
	public String getCuteName() {
		return myCuteLocalName;
	}
	public Address getAddress() {
		return myPublishedAddress;
	}
	
	public String toString() {
		return "Handle[cute=" + myCuteLocalName + ", address=" + myPublishedAddress + "]";
	}
	
	public void dumpDebug() throws Throwable {
		theLogger.debug(this.toString());
	}
}

