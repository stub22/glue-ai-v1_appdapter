package	net.peruser.core.process;

import net.peruser.core.name.Address;
import net.peruser.core.environment.Environment;
import net.peruser.core.handle.Handle;
import net.peruser.core.handle.HandleDirectory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 
public abstract class AbstractProcessorFinder implements ProcessorFinder {

	private static Log 		theLog = LogFactory.getLog(AbstractProcessorFinder.class);
		
	/** */
	public Processor findProcessor(Environment world, String processorClassFQN, String cuteName, 
				Address addr, Data optionalExtraData) throws Throwable {
		
		Class processorClass = Class.forName(processorClassFQN);
		return findProcessor(world, processorClass, cuteName, addr, optionalExtraData);
	}
	/** *
	*/
	protected ProcessorHandle lookupExistingProcessorHandle(Environment env, Address addr) throws Throwable {
		ProcessorHandle result = null;
		HandleDirectory		envPrimaryHD = env.getPrimaryHandleDirectory();
		Handle eh = envPrimaryHD.getHandleForAddress(addr);
		result = (ProcessorHandle) eh;
		return result;
	}
	
	protected ProcessorHandle registerProcessorHandle(Environment env, Processor p,  String cuteName, Address addr) 
					throws Throwable {
		ProcessorHandle resultH = null; 
		HandleDirectory		envPrimaryHD = env.getPrimaryHandleDirectory();
		resultH = new ProcessorHandle(cuteName, addr, p);
		envPrimaryHD.attachHandle(resultH);
		return resultH;
	}
	protected Processor instantiateProcessor(Environment env, Class pClass, Data optionalExtraData) throws Throwable {
		Processor resultP = (Processor) pClass.newInstance();
		resultP.create(env, optionalExtraData);
		return resultP;
	}
}