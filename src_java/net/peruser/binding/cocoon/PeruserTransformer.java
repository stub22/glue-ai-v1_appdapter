package net.peruser.binding.cocoon;

import net.peruser.core.machine.Machine;

/*
import net.peruser.core.document.*;

import net.peruser.core.environment.Environment;
*/


/*
import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;
*/

import org.apache.cocoon.transformation.AbstractDOMTransformer;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Component;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

// Exceptions thrown by setup
import java.io.IOException;
import org.xml.sax.SAXException;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;

/**
* The PeruserTransformer provides access from <a href="http://cocoon.apache.org">
 * Apache Cocoon</a> to our {@link ModelMachine} based processing.
 *
 * We allow multiple instances of this transformer (so we don't mark it "ThreadSafe"),
 * in part because we get part of our per-transaction input in the setup call,
 * the rest in the transform() method.
 * 
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class PeruserTransformer extends AbstractDOMTransformer implements Configurable {
	private static Log 		theLog = LogFactory.getLog(PeruserTransformer.class );
	
	public static String	KERNEL_REBOOT_INSTRUCTION_URI = "peruser:KERNEL_ADMIN/REBOOT";
	public static String	XMLDB_STORE_INSTRUCTION_URI = "peruser:XMLDB/STORE";
	
	private			Configuration	myConfig;
	
	public PeruserTransformer() {
		super();
		theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PeruserTransformer no args constructor called");
	}	
	
	public void configure(Configuration config) throws ConfigurationException {
		try {
			theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% configure(" + hashCode() + ") - START");
			theLog.debug("Configuration is: " + config);
			
			myConfig = config;
			PeruserInputModule.dumpConfig(myConfig);

			theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% configure(" + hashCode() + ") - END");
		} catch (Throwable t) {
			theLog.error("configure caught : ", t);
			throw new ConfigurationException("PeruserTransformer failed in configure()", config);
		} 
	}

	/*
	 * Execute a "transform" action compatible with cocoon, not far removed from JAXP compliant...
	 * <br/>We use an ExcaliburEnvironment and a JenaConfiguredCommandMachine.
	 * <br/>Currently we are creating a new Machine to handle each transformation, which is unnecessary.
	 * <br/>
	 * <br/>The transform relies on the following sitemap parameters:
	 * <br/><b>machine</b>
	 * <br/><b>bootstrap</b>
	 * <br/><b>strippablePrefix</b>
	 */
	protected org.w3c.dom.Document transform(org.w3c.dom.Document w3cInDoc) {

		theLog.info("================ net.peruser.binding.cocoon.PeruserTransformer.transform() - START ===========================");
		theLog.debug("w3cInDoc=" + w3cInDoc);
	
		
		org.w3c.dom.Document w3cOutDoc = null;
		
		try {
			ComponentManager man = manager;
		
			theLog.debug("ComponentManager=" + man);
			Component bigFatKernel = man.lookup(PeruserCocoonKernel.COCOON_ROLE);
			
			PeruserCocoonKernel pck = (PeruserCocoonKernel) bigFatKernel;
			
			theLog.debug("bigFatKernel=" +  bigFatKernel);
			
			theLog.debug("SourceURI=" + source);
			theLog.debug("Parameters=" + parameters);	
		
			Context ctx = ObjectModelHelper.getContext(objectModel);
			
			theLog.debug("Context=" + ctx);

			String transformSourceURI = getTransformationSourceURI();
			if ((transformSourceURI != null) && transformSourceURI.equals(KERNEL_REBOOT_INSTRUCTION_URI)) {
				theLog.info("#########################################################  Rebooting Kernel");
				pck.reboot();
				w3cOutDoc = w3cInDoc;	
			} else 	if ((transformSourceURI != null) && transformSourceURI.equals(XMLDB_STORE_INSTRUCTION_URI)) {
				// TODO: push this code inside deeper
				
			} else {
				w3cOutDoc = pck.transformDOM(myConfig, transformSourceURI, w3cInDoc, parameters, ctx, resolver);
			}
		
		} catch (Throwable t) {
			theLog.error("caught, discarding: ", t);
		}

		theLog.info("=================== net.peruser.binding.cocoon.PeruserTransformer.transform() - END ==========================");
		return w3cOutDoc;
    }
	protected String getTransformationSourceURI() {
		// source is a variable inherited from AbstractDOMTransformer, containing the value of the "src" attribute 
		// from the sitemap.
		return source;
	}
	/** AbstractDOMTransformer is not Configurable, but SQLTransformer is.

	public void configure(Configuration conf) throws ConfigurationException {
		theLog.info("============PeruserTransformer configure() called");
        super.configure(conf);
	}
	*/
    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters parameters) 
			throws ProcessingException, IOException, SAXException {
		theLog.info("============PeruserTransformer setup() called");
		super.setup(resolver, objectModel, source, parameters);	
		// source--          The URI requested
		// This method is called 
	}
}
