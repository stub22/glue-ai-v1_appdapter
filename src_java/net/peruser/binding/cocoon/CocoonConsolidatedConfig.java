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
import net.peruser.core.config.AbstractConfig;
import net.peruser.core.config.MutableConfig;

import net.peruser.core.process.Processor;
import net.peruser.core.process.ProcessorFinder;
import net.peruser.core.process.CoreProcessorFinder;
import net.peruser.core.process.Data;


import net.peruser.core.document.Doc;
import net.peruser.core.document.DocFactory;


// This will be hidden by factory retrieved from String, later

import net.peruser.binding.jena.JenaKernel;
import net.peruser.binding.jena.JenaConfiguredCommandMachine;

import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


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
 * Captures runtime configuration information passed into Peruser from a variety of Cocoon sources 
 * (but, ultimately, all defined using sitemap.xmap and cocoon.xconf) into a single peruser Config,
 * for use by Peruser module implementations.
 *
 * The configuration is "consolidated" in that users access all information using the rdf-like
 * "thing" paradigm (see: ____).  Information sourced from different places (e.g. from transformer
 * "parameters" or from component "configuration") is distinguished to the reader only by the
 * difference in addresses used to look up that information.
 *
 * 
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 public class CocoonConsolidatedConfig extends AbstractConfig {
	 
	private static Log 		theLog = LogFactory.getLog(CocoonConsolidatedConfig.class);

	private 	Configuration	myCocoonConfiguration;
	
	private		Address			myCocooonConfigurationOwnerAddress;
	private		Parameters		myCocoonParameters;
	private		Address			myCocoonParametersOwnerAddress;
	
	/** Create a configuration such that the contents of cocoonConfig are the spots(fields) of a thing at ccOwnerAddress, and
	  * the parameters are the spots(fields) of a thing at parameterOwnerAddress.
	 */
	public CocoonConsolidatedConfig(Configuration cocoonConfig, Address ccOwnerAddress, Parameters parameters, Address parameterOwnerAddress) {
		myCocoonConfiguration = cocoonConfig;
		myCocooonConfigurationOwnerAddress = ccOwnerAddress;
		myCocoonParameters = parameters;
		myCocoonParametersOwnerAddress = parameterOwnerAddress;
	}
  /**
	* Return a list of values bound to a particular spot(field) on a particular thing.
	*/
	public List getFieldValues (Address thingAddress, Address spotAddress) throws Throwable { 
		List resultL = new ArrayList();
		String	fieldAddressLongForm = spotAddress.getLongFormString();
		String pVal = null;
		if (thingAddress.equals(myCocoonParametersOwnerAddress)) {	
			pVal = myCocoonParameters.getParameter(fieldAddressLongForm, null);
		} else if (thingAddress.equals(myCocooonConfigurationOwnerAddress)) {
			// getChild supports namespaces...sorta
			// false -> don't autocreate a new child
			
			Configuration confChild = searchConfigChildrenByFullName(myCocoonConfiguration, spotAddress);
			if (confChild != null) {
				pVal = confChild.getValue();
			}
		}
		if (pVal != null) {
			resultL.add(pVal);
		}
		return resultL;
	}
	/** Namespace support must be enabled in DefaultConfigurationBuilder, which it seems to be by default */
	
	private Configuration searchConfigChildrenByFullName(Configuration parent, Address addr) throws Throwable {
		Configuration resultC = null;
		String	targetFullName = addr.getLongFormString();
		Configuration [] kids = parent.getChildren();
		for (int i=0; i < kids.length; i++) {
			Configuration kid = kids[i];
			String kidFullName = kid.getNamespace() + kid.getName();
			if (kidFullName.equals(targetFullName)) {
				resultC = kid;
				break;
			}
		}
		return resultC;
	}
	/** Find other things which point to thingAddress via spotOnOtherThingAddress
	  */

	public List getBackpointerFieldValues (Address thingAddress, Address spotOnOtherThingAddress) throws Throwable {
		List resultL = new ArrayList();
		return resultL;
	}
	public MutableConfig makeMutableCloneConfig(Environment env) throws Throwable {
		MutableConfig resultMC = null;
		return resultMC;
	}	
}
