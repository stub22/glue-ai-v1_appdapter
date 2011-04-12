package net.peruser.binding.jena;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import net.peruser.core.command.AbstractCommand;
import net.peruser.core.command.Command;

import net.peruser.core.config.Config;
import net.peruser.core.config.MutableConfig;

import net.peruser.core.document.Doc;
import net.peruser.binding.dom4j.Dom4jDoc;

import net.peruser.core.environment.Environment;

import net.peruser.core.machine.CommandMachine;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;

// import static net.peruser.core.vocabulary.SubstrateAddressConstants.instructionAddress;
// import static net.peruser.core.vocabulary.SubstrateAddressConstants.opConfigRefPropAddress;

import com.hp.hpl.jena.util.FileManager;

/**
 * ModelMachine is an implementation of our Machine interface, using a jena OntologyModel as
 * an <b>overridable</b> configuration source for the machine.   
 * <br/><br/>
 * State absorbed through setup() is maintained until the next call to setup().
 * <br/><br/>
 * A cloned configuration is used during each execution of process(), implying that overrides
 * are discarded at the end of the process() invocation.
 * <p>This class is NOT threadsafe.  Not even a little bit.  Don't multithread it!</p>
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class JenaConfiguredCommandMachine extends CommandMachine {
	private static Log 		theLog = LogFactory.getLog(JenaConfiguredCommandMachine.class);	

	private static Address	PA_ASSEMBLY_URI = new CoreAddress("peruser:prop/assemblyURI");
		
	// private		JenaModelAwareConfig		myBaseConfig;

	/** Override the method from AbstractMachine - this impl does too much at present.*/
	protected void	setCurrentConfig(Config c) throws Throwable {
		JenaModelAwareConfig 	realConfig;
		if (c instanceof JenaModelAwareConfig) { 
			realConfig = (JenaModelAwareConfig) c;
		} else {
			// The nominalRootAddress is managed at the ProcessorMachine level, and is usually set during reconfigure(),
			// which is called from PeruserCocoonKernel, right before it calls this method.
			// Strong evidence that JenaConfiguredCommandMachine is NOT threadsafe!
			Address	nominalRootAddr = getCurrentNominalRootAddress(); 
			String assemblyURI = c.getSingleString(nominalRootAddr, PA_ASSEMBLY_URI);
			theLog.info("found assemblyURI: " + assemblyURI);
			Environment currentEnv = getCurrentEnvironment();
			realConfig = buildConfigUsingJenaAssembler(assemblyURI, currentEnv); 
			// Note that we're presently ignoring everything in the config except the nominalRootAddr.assemblyURI.
			// But...this config is probably a CocoonConsolidatedConfig, and includes all the 
			// transformer-type-config AND the transformer-instance-parameters.
			// So, we're dropping all that on the floor in favor of what's in the assembled model.
			// Which would be OK, except that we support overrides from the input (via the mutable config stuff)
			// so...why don't we support overrides (or defaults) from the sitemap, which is a more likely and
			// suitable source of configuration, isn't it?
		}
		super.setCurrentConfig(realConfig);
	}
	/**
	 * Absorb default configuration from a model stream provided by the environment,  using configPath as the env-specific lookup key.
	 * <br/><br/>
	 * This will overwrite any previous configuration.
	 * <br/><br/>
	 * This "default" configuration will then remain unchanged until the next call to setup().
	 */
	 // Considering changing the configPath type to be an Address.
	public synchronized void setup(String configPath, Environment env) throws Throwable {
		theLog.info("JenaConfiguredCommandMachine.setup() called with configPath: " + configPath);
		setupUsingDirectStream(configPath, env);
		//setCurrentConfig(c);
		theLog.info("JenaConfiguredCommandMachine.setup() complete");
	}
	/** Assume that configPath is a URI that can be assembled by a kernel */
	public synchronized void setupUsingJenaAssembler(String configPath, Environment env) throws Throwable {
		JenaModelAwareConfig jmac = buildConfigUsingJenaAssembler(configPath, env);
		setCurrentConfig(jmac);
	}
	protected synchronized JenaModelAwareConfig buildConfigUsingJenaAssembler(String configPath, Environment env) throws Throwable {
		JenaModelAwareConfig jmac;
		theLog.info("JenaConfiguredCommandMachine.buildConfigUsingJenaAssembler() called with configPath: " + configPath);
		super.setup(configPath, env);
		// If no kernel exists, this will be an unbooted kernel, which won't contain the resource we want.
		JenaKernel  envKernel = JenaKernel.getDefaultKernel(env);
		// This doesn't seem to provide any caching (yet).  Checkin with Jena folks and see what they think!
		Model machineConfigBaseModel = AssemblerUtils.getAssembledModel(envKernel, configPath);
		OntModel machineConfigOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, machineConfigBaseModel);
		theLog.debug("machineConfigOntModel=" + machineConfigOntModel);
		jmac = new JenaModelBackedConfig(machineConfigOntModel);
		return jmac;
	}
	/** Assume that configPath is a path to a resource that can be opened by the environment */
	public synchronized void setupUsingDirectStream(String configPath, Environment env) throws Throwable {
		JenaModelAwareConfig jmac;
		theLog.info("JenaConfiguredCommandMachine.setupUsingDirectStream() called with configPath: " + configPath);
		super.setup(configPath, env);
		
		// Currently we need to choose between one of these two kludges.
		// Just one of several reasons the ModelMachineTest is broken in Peruser 2.1.1.
		// Console kludge
		// String dummyTurtlePath =  env.resolveFilePath("app/testapp/rdf/irrelevant_contents.ttl");
		// Cocoon kludge - compatible with current assumption that PeruserTransformer.SourceResolver is 
		// working relative to in an app subdirectory.
		String dummyTurtlePath =  env.resolveFilePath("../testapp/rdf/irrelevant_contents.ttl");
		
		String resolvedConfigPath = env.resolveFilePath(configPath);
		FileManager fm = FileManager.get();
		
		Model turtleModel = fm.loadModel(dummyTurtlePath);		

		// Model machineConfigBaseModel = FileManager.get().loadModel(modelBaseURI, modelBaseURI, "RDF/XML");
		Model machineConfigBaseModel = fm.loadModel(resolvedConfigPath); // , modelBaseURI, "RDF/XML");
		
		theLog.debug("machineConfigBaseModel=" + machineConfigBaseModel);
		
		OntModel machineConfigOntModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, machineConfigBaseModel);
		/*		
		InputStream	modelInputStream = env.openStream(configPath);


		if (configPath.startsWith("file:")) {
			modelBaseURI = configPath;
		} else {
			modelBaseURI = "file:" + configPath;
		}

		theLog.debug("JenaConfiguredCommandMachine.setupUsingDirectStream() baseURI=" + modelBaseURI);
		OntModel machineConfigOntModel = ModelUtils.loadRDFS_ModelFromStream(modelInputStream, modelBaseURI);
		*/		
		theLog.debug("machineConfigOntModel=" + machineConfigOntModel);
		jmac = new JenaModelBackedConfig(machineConfigOntModel);
		setCurrentConfig(jmac);
		// return jmac;
	} 

}
