package	net.peruser.core.process;

import java.io.Serializable;

/**
 * Data objects are the inputs and outputs of processor operations, and they must sometimes be able to serve
 * as the basis for a config.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 
 
public interface Data extends Serializable {
	public String getTextDump() throws Throwable;
}