package net.peruser.core.document;

import java.io.OutputStream;

import net.peruser.core.config.MutableConfig;
import net.peruser.core.name.Abbreviator;


/**
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public  class DocFactory {

	public static Doc makeDocFromW3CDOM(org.w3c.dom.Document w3cDOM) throws Throwable {
		return net.peruser.binding.dom4j.Dom4jDoc.buildFromW3CDOM(w3cDOM);
	}
	
	public static Doc makeDocFromObject(Object input, boolean throwOnFail) throws Throwable {
		Doc	resultD = null;
		
		if (input instanceof Doc) {
			resultD = (Doc) input;
		} else if (input instanceof org.w3c.dom.Document) {
			org.w3c.dom.Document inDocW3C = (org.w3c.dom.Document) input;
			resultD = makeDocFromW3CDOM(inDocW3C);
		}
		if ((resultD == null) && throwOnFail) {
			throw new Exception("Cannot make net.peruser.core.document.Doc from " + input);
		}
		return resultD;
	}
}
