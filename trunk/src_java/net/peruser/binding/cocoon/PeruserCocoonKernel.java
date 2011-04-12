package net.peruser.binding.cocoon;

import net.peruser.core.process.Processor;

import net.peruser.core.machine.Machine;
import net.peruser.core.environment.Environment;
import net.peruser.binding.console.ConsoleEnvironment;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;
import net.peruser.core.handle.HandleDirectory;
import net.peruser.core.handle.Handle;

import net.peruser.core.config.Config;
import net.peruser.core.process.Processor;
import net.peruser.core.process.ProcessorFinder;
import net.peruser.core.process.CoreProcessorFinder;
import net.peruser.core.process.Data;


import net.peruser.core.document.Doc;
import net.peruser.core.document.DocFactory;


// This will be hidden by factory retrieved from String, later

import net.peruser.binding.jena.JenaKernel;
import net.peruser.binding.jena.JenaConfiguredCommandMachine;
// import net.peruser.module.faceebo.FaceeboMachine;

import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;

import org.apache.avalon.framework.activity.Initializable;

import org.apache.avalon.framework.component.Component;

import org.apache.avalon.framework.thread.ThreadSafe;


import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The PeruserCocoonKernel is a singleton instantiated within a Cocoon Webapp.
 * All peruser-aware Cocoon components (including PeruserTransformer and PeruserCocoonCronJob)
 * are coordinated using this "kernel" singleton instance.  Connections with other major peruser bindings
 * (such as the Jena binding, represented by the JenaKernel) are mediated by this kernel.
 *
 * The fact that PeruserCocoonKernel implements ThreadSafe instructs cocoon to be sure there is only one instance. 
 * It also causes the instance to be created and configured when the Webapp starts up, rather than on-demand (which is the
 * behavior when Threadsafe is not implemented).  Not sure that this "early startup" behavior is guaranteed by contract,
 * though.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 public class PeruserCocoonKernel implements Component, Contextualizable, Configurable, Initializable, ThreadSafe {
	
	// This constant is our lookup key with the ComponentManager and ServiceManager.
	// It needs to match the "role" attribute in the cocoon.xconf
	// [	which is generated based on our patch configuration in   
	// PERUSER_HOME/conf/cocoon/pre_build/xconf_patch/peruser_kernel.xconf  ]
	
	protected static String		COCOON_ROLE = "BIG_FAT_ROLE";		
	 
	private static Log 		theLog = LogFactory.getLog(PeruserCocoonKernel.class );
	
	// Keeping things around here REAL SIMPLE for now
	private		Processor		myIndoorProcessor;
	
	private		Environment		myRootEnvironment;
	
	private 	Configuration	myIndoorKernelConfig;
	
	private		int				myInitCount = 0;
	
	public PeruserCocoonKernel() {
		super();
		theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PeruserCocoonKernel no args constructor called for " + hashCode());
	}
	
	public synchronized void contextualize(org.apache.avalon.framework.context.Context ctx) throws ContextException {
		theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% contextualize(" + hashCode() + ") - START");
		// Start with a console environment - chain sourceResolver holding environments to it later

		myRootEnvironment = new CocoonServletEnvironment(ctx);
		theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% contextualize(" + hashCode() + ") - END");
	}

	public synchronized void configure(Configuration config) throws ConfigurationException {
		try {
			theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% configure(" + hashCode() + ") - START");
			theLog.debug("Configuration is: " + config);
			PeruserInputModule.dumpConfig(config);
			
			// These names must match up with the XML tag names within the <component> in peruser_kernel.xconf
			myIndoorKernelConfig = config.getChild("indoor_kernel");

		} catch (Throwable t) {
			theLog.error("configure caught : ", t);
			throw new ConfigurationException("PeruserCocoonKernel failed in configure()", config);
		} 
	}
	
	public synchronized void initialize() throws Exception {
		theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% initialize(" + hashCode() + ") - START");
		try {
			if (myInitCount > 0) {
				theLog.warn("initialize called for the " + (myInitCount + 1) + " time on this PeruserCocoonKernel - ignoring!!!" );
			} else {
				// These names must match up with the XML tag names within the <component> in peruser_kernel.xconf
				String indoorClassname = getChildConfigStringValue(myIndoorKernelConfig, "classname", true);
				String indoorBootstrap = getChildConfigStringValue(myIndoorKernelConfig, "bootstrap", true);
				String indoorCuteName = getChildConfigStringValue(myIndoorKernelConfig, "cuteName", true);
				String indoorPubURI = getChildConfigStringValue(myIndoorKernelConfig, "pubURI", true);
		
				Address indoorPubAddress = new CoreAddress(indoorPubURI);			
				
				String resolvedBootstrap = myRootEnvironment.resolveFilePath(indoorBootstrap);
				
				// Constructing the kernel automatically places it in the supplied environment, so it can be looked up later.
				JenaKernel jk = new JenaKernel(myRootEnvironment, resolvedBootstrap, indoorCuteName, indoorPubAddress);
				
				jk.dumpDebug();
			}
		} catch (Throwable t) {
			theLog.error("initialize caught : ", t);
			throw new CascadingRuntimeException("PeruserCocoonKernel failed in initialize()", t);
		}  
		myInitCount++;
		theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% initialize(" + hashCode() + ") - END");
	}
	public synchronized void shutdown() throws Throwable {
		HandleDirectory rootDir = myRootEnvironment.getPrimaryHandleDirectory();
		String indoorCuteName = getChildConfigStringValue(myIndoorKernelConfig, "cuteName", true);
		Handle indoorKernelHandle = rootDir.getHandleForCuteName(indoorCuteName);
		rootDir.detachHandle(indoorKernelHandle);
		myInitCount = 0;
	}
	public synchronized void reboot() throws Throwable {
		theLog.info("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR reboot(" + hashCode() + ") - START");
		shutdown();
		initialize();
		theLog.info("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR reboot(" + hashCode() + ") - END");
	}
	private String getChildConfigStringValue (Configuration parentConf, String childName, boolean throwOnFail) throws Exception {
		String result = null;
		Configuration 	childConf = parentConf.getChild(childName, false);
		if (childConf != null) {
			result = childConf.getValue();
		} 
		if ((result == null) && throwOnFail) {
			throw new Exception("Expected config tag " + childName + " (in namespace ?) at " + childConf.getLocation());
		}
		return result;
	}
	
	protected synchronized Processor getConfiguredProcessor(Environment pEnv, String pClassFQN, String pCuteName, Address pAddr, 
					Config pConf, Data pOptionalExtraData) throws Throwable {
		Processor resultP = null;
		ProcessorFinder pf = new CoreProcessorFinder();
		resultP = pf.findProcessor(pEnv, pClassFQN, pCuteName, pAddr, pOptionalExtraData);
		// Under what conditions should this be done?
		resultP.reconfigure(pConf, pEnv, pAddr); 
		return resultP;
	} 	
	
	/**
	  *  Note that all parameters and results are cocoon-specific objects, not peruser objects.
	  *
	  *  The transformSourceURI and parameter names must all be either absolute-URI-strings OR 
	  *  qNames transformable to Addresses by [the xformConfig?].
	  *
	  *  The xformConfig must contain a machine configuration block, specifying at least a cuteName and a class (java class)
	  *  for a machine that is to be used to perform the transformation.
	  *
	  *  Note that this method is <b/>not synchronized</b>, and must invoke only thread-safe functionality.
	  */
	public org.w3c.dom.Document transformDOM(Configuration xformConfig, String transformSourceURI, org.w3c.dom.Document w3cInDoc, 
					Parameters parameters, Context ctx, SourceResolver resolver) throws Throwable {
	
		org.w3c.dom.Document	w3cOutDoc = null;
		
		String transformerName = xformConfig.getAttribute("name");
		Configuration machineConf = xformConfig.getChild("machine", false);
		if (machineConf == null) {
			// Should search for command config in kernel boot model?
			throw new Exception ("Can't find machine config for transformer " + transformerName + " at " + xformConfig.getLocation());
		}
		String machineCuteName = getChildConfigStringValue(machineConf, "cuteName", true);	
		if (machineCuteName == null) {
			raiseProcessingException("transformDOM", "machineCuteName=null",  
						" Check pm:cuteName in the (sitemap) transformer component definition for " + transformerName );
		}		
		String machineClassFQN = getChildConfigStringValue(machineConf, "class", true);
		if (machineClassFQN == null) {
			raiseProcessingException("transformDOM", "machineCuteName=null",  
						" Check pm:class in the (sitemap) transformer component definition for " + transformerName);
		}				
		
		// resolver is a (protected scope) SourceResolver set in the setup() call implemented by AbstractDOMTransformer
		// Excalibur stuff is being replaced by Spring in Cocoon 2.2, so we will need to revisit this code.
		Environment env = new ExcaliburEnvironment(myRootEnvironment, resolver, null); // strippablePrefix);
		
		if (transformSourceURI == null) {
			raiseProcessingException("transformDOM", "transformSourceURI=null",  
					" Check src attribute in your (sitemap) pipeline transformer instance of type " + transformerName);
		}
		CoreAddress instructAddr = new CoreAddress(transformSourceURI);
		
		String dummyFullURI = "peruser:PROCESSOR_" + machineCuteName;
		Address addr = new CoreAddress(dummyFullURI);
		
		Config pConf = new CocoonConsolidatedConfig(machineConf, addr, parameters, instructAddr);
		
		Data extraData = null;

		Doc inDoc = DocFactory.makeDocFromW3CDOM(w3cInDoc);
		
		Processor p = getConfiguredProcessor(env, machineClassFQN, machineCuteName, addr, pConf, extraData);
		
		Data inData = inDoc;
		
		Data outData = p.process(instructAddr, inData, env);  
		
		Doc outDoc = (Doc) outData;
		
		w3cOutDoc = outDoc.getW3CDOM();
		
		return w3cOutDoc;
		
	}
	protected void raiseProcessingException(String function, String problem, String suggestion) throws Throwable {
		String className = this.getClass().getName();
		String message = className + "[" + "singleton?" + "]." + function + " encountered '" + problem + "'; suggestion: " + suggestion;
		throw new Exception(message);
	}	
	
}
