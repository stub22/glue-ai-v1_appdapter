package net.peruser.test.data;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAbbreviator;

import java.util.Map;
import java.util.HashMap;

/** Unit testing default data contents, which are sometimes overridden with dynamic configuration data.
 *
 * @author      Stu Baurmann
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
