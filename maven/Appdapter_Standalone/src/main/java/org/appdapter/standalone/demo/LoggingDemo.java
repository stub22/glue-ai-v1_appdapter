package org.appdapter.standalone.demo;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prove that we can use Log4J via SLF4J, resolve log4j.properties, etc.
 * 
 * The default behavior of Log4J is to look for a log4j.properties classpath resource,
 * in the default (root) package, using the current thread's ThreadContext classLoader
 * (where I think "current thread" is whatever thread first actually tries to write
 * to Log4J).
 * 
 * jena-2.6.4.jar contains such a log4j.properties file.  It is the only current dependency 
 * which does.  This means that Appdapter_Standalone will use jena's log4j.properties,
 * unless it supplies its own, which in fact it currently does.  Appdapter_Standalone's
 * copy of log4j.properties is AHEAD of jena's in the classpath, so it is currently
 * the one that is used.  
 * 
 * If we exclude the jena dependency, AND remove the log4j.properties resource
 * from this project, then the output from our main will be:
 * 
[System.out] OK so far...
log4j:WARN No appenders could be found for logger (org.appdapter.standalone.demo.LoggingDemo).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
[System.out] ...all done!
 * 
 * 
 * Also note:
 * 
 
You can specify location with VM argument like this: 
-Dlog4j.configuration="file:/C:/workspace3/local/log4j.properties"
 
 */
public class LoggingDemo {
	private static Logger 		theLogger = LoggerFactory.getLogger(LoggingDemo.class);		
	
	private static String L4JP = "log4j.properties";
	

    public static void main( String[] args ) {
        System.out.println("[System.out] OK so far...");
		debugLoaders();
		
		theLogger.info("I have so much to tell you!");
		theLogger.debug("Debuggery!");
		theLogger.warn("Warning!  (Did you see Debuggery?)");
		theLogger.trace("Say hi to trace-y for me");
		System.out.println("[System.out] ...all done!");
	
    }
	public static void debugLoaders() { 
		ClassLoader myLoader = LoggingDemo.class.getClassLoader();
		ClassLoader pLoader = myLoader.getParent();
		ClassLoader gpLoader = pLoader.getParent();
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		ClassLoader systemCL = ClassLoader.getSystemClassLoader();

		System.out.println("Classloader for the main class is: " + myLoader);
		System.out.println("Parent Classloader is: " + pLoader);
		System.out.println("GrandParent Classloader is: " + gpLoader);
		System.out.println("ContextClassloader for currentThread is: " + threadCL);
		System.out.println("SystemCL is: " + systemCL);

		debugResolve("MainClass", myLoader, L4JP);
		debugResolve("Parent", pLoader, L4JP);

		/*Calls class.resolveName, which 
		  * Add a package name prefix (of the class answering getResource()) if the name is not absolute (no leading /)
		 *  OR 
		  * Remove leading "/" if name is absolute
          */
		String absPath = "/" + L4JP;
		URL ezResolvedURL = LoggingDemo.class.getResource(absPath);
		System.out.println("LoggingDemo.class.getResource resolved " + absPath + " to URL: " + ezResolvedURL);		
	}
	public static void debugResolve(String desc, ClassLoader cl, String resourcePath) {
		System.out.println("------------");
		URL rurl = cl.getResource(resourcePath);
		System.out.println("Classloader[" + desc + ", " + cl + "].getResource(" + resourcePath + ") = " + rurl);
		URL surl = cl.getSystemResource(resourcePath);
		System.out.println("Classloader[" + desc + ", " + cl + "].getSystemResource(" + resourcePath + ") = " + surl);
		System.out.println("------------");		
	}	
}
