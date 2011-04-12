package net.peruser.core.machine;

import net.peruser.core.environment.Environment;
import net.peruser.core.config.Config;
import net.peruser.core.name.Address;

// import net.peruser.core.document.Doc;

/**
 * AbstractMachine provides rudimentary bookeeping for certain Machine assets.
 * TODO:  Make "Observable" for monitoring/administration.
 * TODO:  Add some nice logging.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class AbstractMachine implements Machine {
	private		Environment		myCurrentEnvironment;
	private		Config			myCurrentConfig;
	
	protected void	setCurrentEnvironment(Environment e) {
		myCurrentEnvironment = e;
	}
	protected void	setCurrentConfig(Config c) throws Throwable {
		myCurrentConfig = c;
	}
	public Environment getCurrentEnvironment() {
		return myCurrentEnvironment;
	}
	public Config getCurrentConfig() {
		return myCurrentConfig;
	}
	
	/**
	  *		Sets the environment only, does not do anything about config+instruction.
	  */
	public void setup(String configPath, Environment env) throws Throwable {
		myCurrentEnvironment = env;
	}

}		
