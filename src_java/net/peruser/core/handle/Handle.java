package net.peruser.core.handle;

import net.peruser.core.name.Address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**   A Handle is a runtime reference to a computing resource.
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */

public class Handle {
		
	private static Log 		theLog = LogFactory.getLog(Handle.class);	
	
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
		theLog.debug(this.toString());
	}
}

