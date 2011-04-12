package net.peruser.binding.cocoon;

import net.peruser.core.environment.Environment;
import net.peruser.core.environment.AbstractEnvironment;

import org.apache.avalon.framework.context.ContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class CocoonServletEnvironment extends AbstractEnvironment {
	private static Log 		theLog = LogFactory.getLog(CocoonServletEnvironment.class);
	
	private		String				myContextRootURL;
	
	public  CocoonServletEnvironment(org.apache.avalon.framework.context.Context ctx) throws ContextException {
		super();
		java.net.URL rootURL = (java.net.URL) ctx.get("context-root");
		myContextRootURL = rootURL.toExternalForm();
		theLog.info("%%%%%%%%%%%%%%%%%%%%  Found context-root: " + myContextRootURL);
	}
	public String resolveFilePath (String rawPath) throws Throwable {
		String result = myContextRootURL + rawPath;
		theLog.debug ("resolveFilePath() resolved " + rawPath + " to " + result);
		return result;
	}
}
