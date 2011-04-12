package	net.peruser.core.process;

import net.peruser.core.name.Address;
import net.peruser.core.environment.Environment;
import net.peruser.core.handle.Handle;
import net.peruser.core.handle.HandleDirectory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Objects of this class are stateless internally, but they do read/write processorHandles in the world directory.
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 
public class CoreProcessorFinder extends AbstractProcessorFinder {
	private static Log 		theLog = LogFactory.getLog(CoreProcessorFinder.class);
	/* Finds or instantiates a processor, but does not configure it or otherwise mess with it */
	public Processor findProcessor(Environment env, Class pClass, String cuteName, Address addr,
					Data optionalExtraData) throws Throwable {
	
		ProcessorHandle resultPH = null;
		Processor		resultP;
		resultPH = lookupExistingProcessorHandle(env, addr);
		String message = "HUH";

		if (resultPH != null) {
			resultP = resultPH.getProcessor();
			message = "found existing processor";
		} else {
			resultP = instantiateProcessor(env, pClass, optionalExtraData);
			resultPH = registerProcessorHandle(env, resultP, cuteName, addr);
			message = "instantiated new processor";
		}
		String stats = " at [" + resultPH + "]=[" + resultP + "]";
		theLog.info(message + " " + stats);		
		return resultP;
	}

}