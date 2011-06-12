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

package com.appdapter.peru.test.data;

import com.appdapter.peru.core.name.Address;
import com.appdapter.peru.core.name.CoreAbbreviator;

import java.util.Map;
import java.util.HashMap;

/** Unit testing default data contents, which are sometimes overridden with dynamic configuration data.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class TestDataConstants {

	public static class ProjectorUnitTestConstants {
		public static final String DATA_SRC="app/testapp/rdf/lu_web_p31_121.owl";
		public static final String TEST_NS="http://www.logicu.com/web.owl#";
		public static final String RDFS_NS="http://www.w3.org/2000/01/rdf-schema#";
		public static final String RDFS_COMMENT_URI = RDFS_NS + "comment";
		public static final String RDFS_SUBCLASS_URI = RDFS_NS + "subClassOf";
		public static final String TEST_CONTAINS_URI = TEST_NS + "contains";
		public static final String TEST_FOF_URI = TEST_NS + "folder_of_folders";
		public static final String TEST_THINGY_URI = TEST_NS + "Thingy";
		
		public static final int		MAX_CHILD_LEVELS = 3;
	}
	public static class SPARQL_UnitTestConstants {
		public static final String SUT_queryFileURL = "app/toolchest/sparql/fancy_reasoners.sparql";
		public static final String SUT_modelURL = "app/toolchest/rdf/sw_tools_070619_utf8.rdf";
		public static final String SUT_modelBaseURI = "http://www.peruser.net/phonyBaseURI#";
		public static final String SUT_modelFormat = "RDF/XML";
	}
	public static class ModelMachineUnitTestConstants {
		public static final String  MMT_defaultCommandPath = "app/testapp/rdf/tropic_fetch_cmds.owl";
		public static final String  MMT_defaultParamURL = "file:app/testapp/xml/machine_param_test.xml";
	}
	
}
