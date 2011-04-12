package net.peruser.core.document;

import java.io.OutputStream;

import net.peruser.core.config.MutableConfig;
import net.peruser.core.name.Abbreviator;

import net.peruser.core.process.Data;


/**
 * Doc is currently hardwired to wrap around a dom4j doc.
 * This presents a certain handicap in representing small literals as Docs.
 * So, we plan to make the representation more flexible, later.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class Doc implements SentenceValue, Data {


	public abstract void writePretty (OutputStream outStream) throws Throwable;
	
	public abstract Doc transform(String xformPath, String systemID) throws Throwable;
	
	public abstract org.w3c.dom.Document getW3CDOM() throws Throwable;		
	
	public abstract void applyOverrides (MutableConfig conf, Abbreviator abbr) throws Throwable;

}
