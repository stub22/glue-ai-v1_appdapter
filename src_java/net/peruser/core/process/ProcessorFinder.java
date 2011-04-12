package	net.peruser.core.process;

import net.peruser.core.name.Address;
import net.peruser.core.environment.Environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 
public interface ProcessorFinder {
	/* Maximum flexibility, minimum convenience */
	public Processor findProcessor(Environment world, Class processorClass, String cuteName, Address processorAddress, 
	  			Data optionalInputData) throws Throwable;

	/* Maximum convenience for quick tests, minimum flexibility. */  
	public Processor findProcessor(Environment world, String processorClassFQN, String cuteName, Address processorAddress,
				Data optionalInputData) throws Throwable;
  
}