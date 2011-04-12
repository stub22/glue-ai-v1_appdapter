package net.peruser.binding.cocoon;

import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.cron.ServiceableCronJob;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.component.Component;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class PeruserCocoonCronJob extends ServiceableCronJob implements Configurable{
	
	private static Log 		theLog = LogFactory.getLog(PeruserCocoonCronJob.class );
	// protected variable "manager" of type 
	
	public PeruserCocoonCronJob() {
		super();
		// Note we are using the static apache commons interface here, rather than the avalon interface, as fetched below...
		// ( "getLogger()" fails in the constructor).
		theLog.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PeruserCocoonCronJob no args constructor called");
	}	

    public void execute(String name) {
        getLogger().info("pc-CronJob " + name + " launched at " + new Date());
		try {
			// manager is a ServiceManager - not a ComponentManager like we get in the InputModule and Transformer!
			Component bigFatKernel = (Component) manager.lookup(PeruserCocoonKernel.COCOON_ROLE);
			getLogger().debug("bigFatKernel=" +  bigFatKernel);
			
			if (name.equals("config-reload")) {
			}
        	getLogger().info("pc-CronJob " + name + " completed normally at " + new Date());
        } catch(Throwable t) {
            throw new CascadingRuntimeException("CronJob " + name + " raised an exception", t);
        } finally {

        }
	}
	
	public void configure(Configuration config) throws ConfigurationException {
		try {
			getLogger().info("%%%%%%%%%%%%%%%%%%%PeruserCocoonCronJob%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% configure(" + hashCode() + ") - START");
			getLogger().debug("PeruserCocoonCronJob Configuration is: " + config);
			PeruserInputModule.dumpConfig(config);
			getLogger().info("%%%%%%%%%%%%%%%%%%%%%%%%%%%PeruserCocoonCronJob%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% configure(" + hashCode() + ") - END");
		} catch (Throwable t) {
			getLogger().error("configure caught : ", t);
			throw new ConfigurationException("PeruserCocoonCronJob failed in configure()", config);
		} 
	}	
}
