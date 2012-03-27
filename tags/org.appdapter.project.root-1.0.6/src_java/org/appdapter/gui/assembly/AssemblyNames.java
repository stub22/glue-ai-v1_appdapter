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

package org.appdapter.gui.assembly;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class AssemblyNames {

	public static	String		NS_rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static 	String		NS_rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	public static	String		NS_dc = "http://purl.org/dc/elements/1.1/";
	public static	String		NS_box = "http://www.appdapter.org/schema/box#";

	public static 	String		P_label			= NS_box + "label";
	public static 	String		P_description	= NS_rdfs + "description";
	public static 	String		P_javaFQCN		= NS_box + "javaFQCN";
	public static 	String		P_trigger		= NS_box + "trigger";
	
	public static 	String		P_extraThing	= NS_box + "extraThing";
}
