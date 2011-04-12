package net.peruser.core.command;

import net.peruser.core.config.Config;

import net.peruser.core.environment.Environment;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// JDK 1.5 constants import - POW!
import static net.peruser.core.vocabulary.SubstrateAddressConstants.*;

/**  Implementation assumptions of our simple demo Command processing system are captured here.
 * This class does NOT implement the execute() method of the Command interface.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class AbstractCommand implements Command {
	private static Log 		theLog = LogFactory.getLog(AbstractCommand.class);
	
	protected		Environment		myEnvironment;
	protected		Config			myConfigImpl;
	protected		Address			myCommandAddress;
	
	/** This default implementation of close() does nothing. */
	public void close() throws Throwable {
	}
	
	public void configure (Environment env, Config config, Address cmdInstanceAddress) throws Throwable {
		// These things are statics, so this per-instance init is braindead.  Revisit.

		myEnvironment = env;
		myConfigImpl = config;
		myCommandAddress = cmdInstanceAddress;
	}
	public static Command instantiateAndConfigure (Environment env, Config config, Address cmdInstanceAddress) 
				throws Throwable {
		
		String javaClassName = getCommandClassName(config, cmdInstanceAddress);
		
		theLog.debug("commandClassName is " + javaClassName);

		Class commandClass = Class.forName(javaClassName);
		Command command = (Command) commandClass.newInstance();
		command.configure(env, config,  cmdInstanceAddress);	
		return command;
	}
	
	public Address getCommandAddress() {
		return myCommandAddress;
	}
	
	public Address resolveRef (Address ref) throws Throwable {
		String ident =  myConfigImpl.getOptionalString (ref, identPropAddress);
		if (ident == null) {
			ident = "";
		}
		Address spaceAddress =  myConfigImpl.getSingleAddress (ref, spacePropAddress);
		String spaceIdent = myConfigImpl.getSingleString(spaceAddress, identPropAddress);
		
		Address resolved = new CoreAddress (spaceIdent + ident); // OLD:  new Address (spaceIdent + "#" + ident);
		return resolved;
	} 

	public static String getCommandClassName (Config config, Address commandInstanceAddress) throws Throwable {
		Address handlerConfig = config.getSingleAddress(commandInstanceAddress, handlerConfigPropAddress);
		String javaClassName = config.getSingleString(handlerConfig, javaClassNamePropAddress);
		return javaClassName;
	}
	protected String mapPlaceURL (String rawPath) throws Throwable {
		String result = rawPath;
		if (myEnvironment != null) {
			// Need to rename this method.
			result = myEnvironment.resolveFilePath(rawPath);
		}
		return result;
	}
	
	protected String getRawPlaceURL (Address placeDescAddress) throws Throwable {
		String placeURL_String = null;
		// locationPath may be an absolute URL, a name/path relative to some ambient context, or a name/path relative to an explicit repository
		String  locationPath = myConfigImpl.getSingleString(placeDescAddress, locationPathPropAddress);
		theLog.debug("locationPath is " + locationPath);	
		Address repositoryDescAddress = myConfigImpl.getOptionalAddress(placeDescAddress, repositoryPropAddress);
		if (repositoryDescAddress != null) {
			// Note that the URL value is an address/resource/URI, not a string!
			Address repositoryURL_Address = myConfigImpl.getSingleAddress(repositoryDescAddress, urlPropAddress);
			theLog.debug("repositoryURL_Address is " + repositoryURL_Address);
			String repositoryURL_String = repositoryURL_Address.getResolvedPath();
			theLog.debug("repositoryURL_String is " + repositoryURL_String);			
			placeURL_String = repositoryURL_String + locationPath;
		} else {
			placeURL_String = locationPath;
		}
		return placeURL_String;
	}
	
	protected String getMappedPlaceURL (Address placeDescAddress) throws Throwable {
		String rawPlaceURL = getRawPlaceURL (placeDescAddress);
		String mappedPlaceURL = mapPlaceURL(rawPlaceURL);
		return mappedPlaceURL;
	}


}

