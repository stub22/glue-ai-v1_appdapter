package	net.peruser.core.process;


import net.peruser.core.handle.Handle;
// import net.peruser.core.config.Config;
import net.peruser.core.name.Address;
// import net.peruser.core.environment.Environment;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 
public class ProcessorHandle extends Handle {
	
	private	Processor	myProcessor;

	public ProcessorHandle(String cuteName, Address addr, Processor p) {
		super(cuteName, addr);
		myProcessor=p;
	}
	public Processor getProcessor() throws Throwable {
		return myProcessor;
	}
}