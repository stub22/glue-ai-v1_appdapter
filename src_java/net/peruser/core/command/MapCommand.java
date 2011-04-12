package net.peruser.core.command;

import net.peruser.core.config.Config;

import net.peruser.core.document.Doc;

import java.util.Map;

import net.peruser.core.environment.Environment;

import net.peruser.core.name.Address;
 
/** MapCommand is a Command that knows how to process java.util.Map objects.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class MapCommand extends AbstractCommand {

	public Object work(Object input) throws Throwable {
		Object result = null;
		if (input instanceof Map) {
			Map	inMap = (Map) input; 
			Map outMap = workMap(inMap);
			result = outMap;
		} 
		return result;
	}	
	/**
	  * Execute the one and only Map-based operation that this command exists to process,
	  * making any necessary changes to stored models, docs, etc.
	  */
	  
	protected abstract Map workMap(Map input) throws Throwable;
}
