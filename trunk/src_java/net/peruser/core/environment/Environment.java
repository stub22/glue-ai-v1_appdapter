package net.peruser.core.environment;

import java.io.InputStream;

import net.peruser.core.handle.HandleDirectory;


/** 	An Environment provides access to system resources for processing Machines.
 * 
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface Environment {
	
	/** The resolution mechanism depends on how the Peruser Bindings are set up for this Environment.
	  * @param canonPath	A forward-slash style relative path/like/this.whatever
	  * @return             A system path that can be used to open the resource in this JVM + Peruser.
	  */
	public String resolveFilePath(String canonPath) throws Throwable;
	
	/** Convenience method to call resolveFilePath and then open a JVM InputStream on the result.
	  * @param canonPath	A forward-slash style relative path/like/this.whatever
	  * @return             A stream open to the resource.
	  */
	public InputStream openStream(String canonPath) throws Throwable;
	
	/**
	  *  Return the "primary" directory of handles "known" in this environment. 
	  */
	public HandleDirectory getPrimaryHandleDirectory() throws Throwable;
	 
}
