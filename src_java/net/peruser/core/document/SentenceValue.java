package net.peruser.core.document;

import net.peruser.core.config.Config;

/**
 * A SentenceValue is something that can format itself for use as representation of a
 * value used in a logical sentence in some category of Configs.  For example, if this 
 * SV can produce an RDF-Literal, then it can be used to produce such a literal for any 
 * RDF model mentioned by a config.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface SentenceValue {
	public Object getCompatibleValue (Config conf) throws Throwable;
}
