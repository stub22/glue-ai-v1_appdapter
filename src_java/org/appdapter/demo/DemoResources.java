/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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
package org.appdapter.demo;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoResources {
	static Logger theLogger = LoggerFactory.getLogger(DemoResources.class);
	
	// The leading slash format is appropriate when resources are resolved into a URL by Class.getResource(),
	// but not when we want a classloader to find them directly.  
	
	//		1) Use "" for classloader - best for use by developers, testers, and in many real deploys.
	//	OR	2) Use a Jena-FM friendly URL like "file:/x/y/z/" - The classpath is frozen or hurtin, and you need a darn file!
	//	OR	3) Use "/" and then pre-resolve these paths using MyClass.class.getResource(), but jar:file URLs won't work
	//		with Jena FM.  You can instead open streams to the files yourself, and use alternate Jena APIs with
	//		those streams.
	
	public static String OPTIONAL_ABSOLUTE_ROOT_PATH = ""; 
	public static String DEMOCONF_ROOT_PATH = OPTIONAL_ABSOLUTE_ROOT_PATH + "org/appdapter/democonf"; // RPPATH = Root
	
	public static String STORE_CONFIG_PATH = DEMOCONF_ROOT_PATH + "/store/appdemo_sdb_h2.ttl";
	
	
	public static String DATA_PATH = DEMOCONF_ROOT_PATH + "/owl/snazzy.owl";
	public static String QUERY_PATH = DEMOCONF_ROOT_PATH + "/sparql/query_stuff.sparql";	
	public static String MENU_ASSEMBLY_PATH = DEMOCONF_ROOT_PATH + "/app/boxdemo/boxy_001.ttl";
	
	/**
	//  It seems that Jena 2.6.4  FileManager builtin Locators cannot handle a JAR-embedded URL like:
	//  jar:file:/C:/Users/winston/.m2/repository/org/appdapter/Appdapter_ScalaAndJava/1.0-SNAPSHOT/Appdapter_ScalaAndJava-1.0-SNAPSHOT.jar!/org/appdapter/democonf/app/boxdemo/boxy_001.ttl
	//  So instead we must rely on Jena's classpath loader, which means this method is kinda worse than useless
	// (cuz it works great when resources are in file:myproj/classes/mypkg/thing.txt, but not after Jar-ing same project).

	 * 
	 */

	public static String makeURLforClassNeighborResPath_JenaFMCantUseButModelReaderCan(Class neighbor, String resURL_path) {
		if (!resURL_path.startsWith("/")) {
			theLogger.warn("Relative path class.getResource(" + resURL_path + ") will receive converted package prefix for: " + neighbor.getPackage().getName());
		}
		URL resURL = neighbor.getResource(resURL_path);
		theLogger.debug(neighbor.toString() + " resolved " + resURL_path + " to " + resURL);
		return resURL.toString();
	}	
	public static String makeURLforClassLoaderResPath_JenaFMCantUseButModelReaderCan(ClassLoader classLoader, String resURL_path) {
		URL resURL = classLoader.getResource(resURL_path);
		theLogger.debug(classLoader.toString() + " resolved " + resURL_path + " to " + resURL);
		return resURL.toString();
	}
	
	
}
