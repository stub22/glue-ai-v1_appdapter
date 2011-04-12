package net.peruser.binding.console;

import net.peruser.core.environment.AbstractEnvironment;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ConsoleEnvironment extends AbstractEnvironment {
	public  ConsoleEnvironment() {
	}
	public String resolveFilePath (String rawPath) throws Throwable {
		return rawPath;
	}
}
