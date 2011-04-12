package net.peruser.core.config;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import net.peruser.core.document.Doc;
import net.peruser.core.document.SentenceValue;

import net.peruser.core.name.Address;



/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class AbstractConfig implements Config {

	private static Log 		theLog = LogFactory.getLog(AbstractConfig.class);
	
	/*
	 * This method may be overriden.
	 */
	protected SentenceValue sentenceValueFor(Object o) {
		SentenceValue result = null;
		if (o instanceof SentenceValue) {
			result = (SentenceValue) o;
		}
		return result;
	}

	protected final Object getSingleValue (Address thing, Address field, 
				boolean searchBackwards, boolean optional) throws Throwable  {
		List valList = null;
		if (!searchBackwards) {
			valList = getFieldValues (thing, field);
		} else {
			valList = getBackpointerFieldValues (thing, field);
		}
		if (valList.size () == 1) {
			return valList.get(0);
		} else if (optional) {
			return null;
		} else {
			dumpConfigThing (thing);
			throw new Exception ("Expected to find 1 value at " + thing.getResolvedPath() + "." + 
					field.getResolvedPath() + " -  but found " + valList.size() + " values");
		} 	
	}
	public final String getSingleString(Address thing, Address field) throws Throwable {
		return (String) getSingleValue(thing, field, false, false);
	}
	public final String getOptionalString(Address thing, Address field) throws Throwable {
		return (String) getSingleValue(thing, field, false, true);
	}
	public final Address getSingleAddress(Address thing, Address field) throws Throwable {
		return (Address) getSingleValue(thing, field, false, false);
	}
	public final Address getOptionalAddress(Address thing, Address field) throws Throwable {
		return (Address) getSingleValue(thing, field, false, true);
	}
	public final Address getSingleBackpointerAddress(Address thing, Address field) throws Throwable {
		return (Address) getSingleValue(thing, field, true, false);
	}
	
	public final Address getSinglePathTargetAddress(Address thing, Address[] path) throws Throwable {
		Address cursor = thing;
		for (int i=0; i < path.length; i++) {
			cursor = getSingleAddress(cursor, path[i]);
			if (cursor == null) {
				throw new Exception ("Path fetch from " + thing + " failed on token[" + i + "]=" + path[i]);  
			}
		}
		return cursor;
	}
	public void dumpConfigThing(Address thing) {
		theLog.debug("No additional debugging information is available regarding config thing:  " + thing);
	}
}

