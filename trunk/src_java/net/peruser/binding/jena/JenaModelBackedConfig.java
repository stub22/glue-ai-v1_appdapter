package net.peruser.binding.jena;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * JenaModelBackedConfig extends JenaModelAwareConfig with the assumption
 * that configuration should be stored in a simple (non-inferenced,
 * non-ontologized) jena model, constructed when the Config is constructed.
 * <br/>
 * That's it, for now.
 * 
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class JenaModelBackedConfig extends JenaModelAwareConfig {
	
	public JenaModelBackedConfig (Model someModel) throws Throwable {
		Model ourModel  = ModelFactory.createDefaultModel();
		// We copy EVERYTHING from the defaults model, which is overkill.  Revisit later.
		ourModel.add(someModel, false);
		
		ourModel.setNsPrefixes(someModel);
		
		setActiveJenaModel(ourModel);
		
		// System.out.println("Dumping prefixes for opConfig model");
		// dumpPrefixes(ourModel, System.out);
	}
}

