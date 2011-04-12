package net.peruser.core.environment;

import java.io.InputStream;
import java.io.FileInputStream;

import net.peruser.core.handle.HandleDirectory;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class AbstractEnvironment implements Environment {
	
	private		HandleDirectory		myPrimaryHandleDirectory;
	
	public AbstractEnvironment() { }
	
	public AbstractEnvironment(Environment parent) throws Throwable {
		HandleDirectory parentDirectory = parent.getPrimaryHandleDirectory();
		myPrimaryHandleDirectory = parentDirectory;
	}
	
	/*
	 * This implementation uses "resolveFilePath()", and assumes the path is openable with FileInputStream.
	 */
	public InputStream openStream (String canonPath) throws Throwable {
		return new FileInputStream(resolveFilePath(canonPath));
	}
	/**
	 */
	public HandleDirectory getPrimaryHandleDirectory() throws Throwable {
		if (myPrimaryHandleDirectory == null) {
			myPrimaryHandleDirectory = HandleDirectory.getDefaultDirectory();
		}
		return myPrimaryHandleDirectory;
	}
	/**
	 */
	public void setPrimaryHandleDirectory (HandleDirectory hd) throws Throwable {
		if (myPrimaryHandleDirectory != null) {
			throw new Exception("ILLEGAL PERUSER OPERATION: Cannot reset primaryHandleDirectory for environment " + this 
				+ " in peruser version + @PERUSER_VERSION_FROM_ANNOTATION");

		}
		myPrimaryHandleDirectory = hd;
	}
	
}

