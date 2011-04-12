package net.peruser.binding.cocoon;

import java.io.InputStream;

import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;

import net.peruser.core.environment.Environment;
import net.peruser.core.environment.AbstractEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ExcaliburEnvironment extends AbstractEnvironment {
	private static Log 		theLog = LogFactory.getLog(ExcaliburEnvironment.class);
	
	private 	SourceResolver 		mySourceResolver;
	private		String			myStrippablePrefix;

	public  ExcaliburEnvironment(Environment parentEnv, SourceResolver resolver, String strippable) throws Throwable {
		super(parentEnv);
		mySourceResolver = resolver;
		myStrippablePrefix = strippable;
	}
	public String resolveFilePath (String rawPath) throws Throwable {
		String result = null;
		org.apache.excalibur.source.Source src = getExcaliburSource(rawPath);
		String uri = src.getURI();
		mySourceResolver.release(src);
		if (uri.startsWith("file:")) {
			result = uri.substring(5);
		}
		theLog.debug ("resolveFilePath() resolved " + rawPath + " to " + result);
		return result;
	}
	public InputStream openStream (String rawPath) throws Throwable {
		org.apache.excalibur.source.Source src = getExcaliburSource(rawPath);
		String srcURI = src.getURI();
		theLog.debug("openStream() - SourceURI is: " + srcURI);
		InputStream stream = src.getInputStream();
		mySourceResolver.release(src);
		return stream;
	}
	protected Source getExcaliburSource (String rawPath) throws Throwable {
		String strippedPath = rawPath;
		if ((myStrippablePrefix != null) && (rawPath.startsWith (myStrippablePrefix))) {
			strippedPath = rawPath.substring(myStrippablePrefix.length());
		}
		theLog.debug ("Stripped path to be resolved is: " + strippedPath);

		// Note that SourceResolver also has a method:   resolveURI(java.lang.String location, java.lang.String base, java.util.Map parameters) 
		Source src = mySourceResolver.resolveURI(strippedPath);

		return src;
	}
}
/****
	Generally able to work from the directory of the sitemap that called the transformer.
18:44:14 DEBUG ExcaliburEnvironment      :: Stripped path to be resolved is: rdf/camera_data_001.rdf
18:44:14 DEBUG ExcaliburEnvironment      :: Resolved path is: /C:/_logicu/japp/tomcat_5520/webapps/copper/peruser/picky/rdf/camera_data_001.rdf
  ***/

