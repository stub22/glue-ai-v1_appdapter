package net.peruser.binding.cocoon;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;

import org.apache.cocoon.components.modules.input.AbstractInputModule;
import org.apache.cocoon.components.CocoonComponentManager;

import org.apache.cocoon.environment.ObjectModelHelper;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Context;

import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * The PeruserCocoonKernel is a singleton instantiated within a Cocoon Webapp.
 * All peruser-aware Cocoon components (including PeruserTransformer and PeruserCocoonCronJob)
 * are coordinated using this "kernel" singleton instance.  Connections with other major peruser bindings
 * (such as the Jena binding, represented by the JenaKernel) are mediated by this kernel.
 * 
 * Marking the class as ThreadSafe means that there is only one instance, which is reused by cocoon.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 public class PeruserInputModule extends AbstractInputModule implements ThreadSafe {
	private static Log 		theLog = LogFactory.getLog(PeruserInputModule.class );
	
	public PeruserInputModule() {
		super();
		theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PeruserInputModule no args constructor called for " +
					this.hashCode());
	}
	/* This config is from cocoon.xconf */
	public void configure(Configuration config) throws ConfigurationException {
		try {
			super.configure(config);
			theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% configure() - START for " + this.hashCode());
			theLog.debug("Config{");
			dumpConfig(config);
			theLog.debug("}");
			theLog.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% configure() - END");
		} catch (Throwable t) {
			theLog.error("configure caught : ", t);
			throw new ConfigurationException("PeruserInputModule failed in configure()", config);
		} 
	}
	/**
	  * 
	  * modeConf is "column's mode configuration from resource description. - This argument is optional." <- seems to always be null for us
	  *  
	  */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
		String result = null;
		try {
			theLog.debug("******************************************************************getAttribute[name=" + name);
			theLog.debug("modeConf{");
			dumpConfig(modeConf);
			theLog.debug("}");
			theLog.debug("objectModel=" + objectModel + "]");
	/*
			Set keySet=objectModel.keySet();
			for (Object key : keySet) {
				theLog.debug("keyClass=" + key.getClass().getName() + ", key.toString()=" + key.toString());
			}
	*/
			Request req = ObjectModelHelper.getRequest(objectModel);
			Response resp = ObjectModelHelper.getResponse(objectModel);
			Context ctx = ObjectModelHelper.getContext(objectModel);
			Long expiry = ObjectModelHelper.getExpires(objectModel);
	
			theLog.debug("request: " + req);
			theLog.debug("response: " + resp);
			theLog.debug("context: " + ctx);		
			theLog.debug("expiry: " + expiry);
			
			// This call succeeds when sitemap processing is active, which it always will be during this call.
			ComponentManager cm = CocoonComponentManager.getSitemapComponentManager();
			Component bigFatKernel = cm.lookup(PeruserCocoonKernel.COCOON_ROLE);
			theLog.debug("bigFatKernel=" +  bigFatKernel);
			
			Date now = new Date();
			
			result ="value_of_" + name + "_at_" + now;

		} catch (Throwable t) {
			theLog.error("getAttribute caught : ", t);
		}
		return result;
    }
	
	static void dumpConfig(Configuration conf) {
		try {
			if (conf==null) {
				theLog.debug("conf=NULL");
				return;
			}
			theLog.debug("conf.name()=" + conf.getName());		
			theLog.debug("conf.namespace()=" + conf.getNamespace());	
			theLog.debug("conf.location()=" + conf.getLocation());
			theLog.debug("conf.value()=" + conf.getValue("DEFAULT VALUE OF THIS CONFIG AS SET IN JAVA CODE"));
			theLog.debug("conf.class =" + conf.getClass().getName());
			String attrNames[] = conf.getAttributeNames();
			theLog.debug("conf.attrNames=" + attrNames);
			if(attrNames != null) {
				for(int i=0; i< attrNames.length; i++) {
					theLog.debug("attrName: " + attrNames[i] + " value: " + conf.getAttribute(attrNames[i], "DEFAULT FOR ATTR#" + i));
				}
			}
			Configuration kids[] = conf.getChildren();
			theLog.debug("conf.children=" + kids);
			if (kids != null) {
				for(int i=0; i< kids.length; i++) {
					dumpConfig(kids[i]);
				}
			}
		} catch (Throwable t) {
			theLog.error("Caught during config dump: ", t);
		}
	}
}
