package net.peruser.test.module.projector;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

import net.peruser.core.environment.Environment;
import net.peruser.binding.console.ConsoleEnvironment;

import net.peruser.binding.jena.JenaPulljector;
import net.peruser.binding.jena.ModelUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import net.peruser.core.document.Doc;
import net.peruser.binding.dom4j.Dom4jDoc;

import net.peruser.module.projector.*;

import static net.peruser.test.data.TestDataConstants.ProjectorUnitTestConstants.*;

/** Unit test for the Projector module.  Not currently JUnit-enabled.  Run using "ant ... test-peruser-Projector".
  * <BR/>
  * Note that this test prints to stdout, but also constructs a ResultDoc to demonstrate Machine-compatibility.
  *
  * <UL>
  *  <LI>PT_TEST_NS = "http://www.logicu.com/web.owl#"</LI>
  *  <LI>PT_DATA_SRC = "substrate/owl/lu_web_p31_121.owl"</LI>
  *  <LI>Queries on rdfs:comment, rdfs:subClassOf, and in the TEST_NS: contains, folder_of_folders, Thingy</LI>
  * </UL>
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ProjectorTest {
	private static Log 		theLog = LogFactory.getLog(ProjectorTest.class);
	
	public static void main(String[] args) {
		PrintStream outPS = System.out;
		outPS.println("ProjectorTest - bulb is warming up!");
		try {
			Doc result = execProjectorUtilsTest(DATA_SRC, outPS);
			outPS.println("============================= Start DOM4J Result ======================================");
			result.writePretty(outPS);
			outPS.println("============================= End   DOM4J Result ======================================");
			
		} catch (Throwable t) {
			t.printStackTrace(outPS);
		}
		outPS.println("ProjectorTest - bulb is cooling down!");
	}
	public static Doc execProjectorUtilsTest (String rdfFileSourcePath, PrintStream outPS) throws Throwable {
		// FileInputStream	fis = new FileInputStream(rdfFileSourcePath);
		// JenaPulljector jp = JenaPulljector.makePulljectorFromModelStream(fis, null);
		
		Environment env = new ConsoleEnvironment();		
		
		Model baseModel = ModelUtils.loadJenaModelUsingJenaFileManager(env, rdfFileSourcePath);
		JenaPulljector jp = JenaPulljector.makePulljectorFromBaseModelAndOntSpec(baseModel, null);
		
		jp.dumpPrefixes(outPS);
		
		List fieldPropUris = new ArrayList();
		fieldPropUris.add(RDFS_COMMENT_URI);
		
		// 1) Find everything "contains"-ed in "folder_of_folders", and grab "rdfs:icomment"s along the way. 
		ProjectedNode fofInstanceNode = ProjectorUtils.doSimpleAxisQuery(jp, TEST_FOF_URI, 
				TEST_CONTAINS_URI, SimpleAxisQuery.PARENT_POINTS_TO_CHILD);
		
		ProjectorUtils.printProjectedTreeAsBothTextAndXML(outPS, fofInstanceNode, fieldPropUris, MAX_CHILD_LEVELS);
		
		// 2) Find the rdfs-SubClass tree rooted at "TEST_NS : Thingy"
		ProjectedNode thingyClassNode = ProjectorUtils.doSimpleAxisQuery(jp, TEST_THINGY_URI, 
				RDFS_SUBCLASS_URI, SimpleAxisQuery.CHILD_POINTS_TO_PARENT);
		
		ProjectorUtils.printProjectedTreeAsBothTextAndXML(outPS, thingyClassNode, fieldPropUris, MAX_CHILD_LEVELS);

		// Show that we can construct a ResultDoc from the subclass query.
		Document dom4jOut = ProjectorUtils.createDom4jDocument (thingyClassNode, fieldPropUris, MAX_CHILD_LEVELS);
		Doc resDoc = new Dom4jDoc (dom4jOut);
		
		return resDoc;
	}

}
