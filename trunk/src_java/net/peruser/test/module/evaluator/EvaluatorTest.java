package net.peruser.test.module.evaluator;


import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import com.hp.hpl.jena.ontology.OntModel;

import net.peruser.module.evaluator.EvaluatorCommand;


/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class EvaluatorTest {

	public static final String LF = System.getProperty("line.separator" );
	
	public static void main(String[] args) {
		System.out.println("EvaluatorTest - inflating!");
		String mpath = "app/toolchest/rdf/toolchestCommands.owl";
		String fslPath = "cmd:TestDataPlace/cmd:locationPath/text()";
		
		try {
		
			Map	nsMap = new HashMap();
			nsMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			nsMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			nsMap.put("cmd",  "http://www.peruser.net/2007/command#");	
		
			OntModel om = EvaluatorCommand.readModel(mpath);
			List results = EvaluatorCommand.query(om, nsMap, fslPath);
			
			System.out.println("Got " + results.size() + " results");
			Iterator pii = results.iterator(); 
			
			while (pii.hasNext()) {
				List resultTermList = (List) pii.next();
				System.out.println("Got " + resultTermList.size() + " terms");
				Iterator rti = resultTermList.iterator();
				while (rti.hasNext()) {
					Object term = rti.next();
					System.out.println (term.toString());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("EvaluatorTest - deflating!");
	}
	

}
