package net.peruser.binding.servlet;

import java.io.InputStream;
import java.io.FileInputStream;

import javax.servlet.ServletContext;

import net.peruser.core.environment.AbstractEnvironment;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ServletEnvironment extends AbstractEnvironment {
	private 	ServletContext		myServletContext;
	public  ServletEnvironment(ServletContext servletContext) {
		myServletContext = servletContext;
	}
	public String resolveFilePath (String rawPath) throws Throwable {
		return myServletContext.getRealPath(rawPath);
	}
}

