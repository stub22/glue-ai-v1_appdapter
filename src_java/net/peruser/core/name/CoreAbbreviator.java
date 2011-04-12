package net.peruser.core.name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.peruser.core.document.SentenceValue;
import net.peruser.core.config.Config;

import java.util.Map;
import java.util.HashMap;

/**
 * CoreAbbreviator is an Abbreviator for working with CoreAddresses.
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class CoreAbbreviator implements Abbreviator {
	
	private static Log 		theLog = LogFactory.getLog(CoreAbbreviator.class);
	
	private		Map<String,String>		myPrefixMap;
	private		String					myShortFormSeparator, myLongFormSeparator;
	
	public CoreAbbreviator (Map<String,String> prefixMap, String shortFormSeparator, String longFormSeparator) {
		myPrefixMap	= prefixMap;
		myShortFormSeparator = (shortFormSeparator != null) ? shortFormSeparator : "";
		myLongFormSeparator = (longFormSeparator != null) ? longFormSeparator : "";
	}
	
	/**
	
	public Object lookupCompatibleValueAtAddress (Config conf, Address a) throws Throwable {
		throw new Exception("CoreAbbreviator cannot lookup configured values");
	}
	*/
	
	public String makeShortFormFromAddress(Address a) {
		return "REVERSE PREFIX LOOKUP NOT IMPLEMENTED YET";
		// ca.getShortPrefix() + myShortFormSeparator + ca.getIdent();
	}
	public String getLongForm(CoreAddress ca) {
		return ca.getLongFormString();
		/*
		String result = null;
		String rawPrefix = ca.getShortPrefix();
		String ident = ca.getIdent();
		*/
	}
	public String resolveAddress(Address a) throws Throwable {
		return a.getLongFormString();
		/*
		String result = null;
		CoreAddress ca = (CoreAddress) a;
		theLog.debug("CoreAbbreviator resolving " + getShortForm(ca));
		result = getLongForm(ca);
		theLog.debug("CoreAbbreviator resolved " + result);
		return result;
		*/
	}
	
	public Address makeAddressFromLongForm(String longForm) throws Throwable {
		return null;
		// parseCoreAddress(resolvedLongForm);
	}

	public String resolveAddressToLongForm(Address a) throws Throwable {
		return null;
	}	
	
	public Address makeAddressFromShortForm (String shortForm) throws Throwable {
		CoreAddress add = null;
		if (shortForm != null) {
			ParsedPair splitPair = ParsedPair.parsePair(shortForm, myShortFormSeparator);
			if (splitPair != null) {
				String rawPrefix = splitPair.left;
				String ident = splitPair.right;
				String mappedPrefix = myPrefixMap.get(rawPrefix);
				if (mappedPrefix == null) {
					throw new Exception("Can't resolve prefix " + rawPrefix + " in short-form address " + shortForm); 
				}
				String longForm = mappedPrefix + myLongFormSeparator + ident;
				add = new CoreAddress (longForm);
			} else {
				throw new Exception("Can't parse short-form address " + shortForm);
			}
		}
		return add;
	}
/**
	Build another CoreAbbreviator identical to this one, but with an additional prefix + expansion pair.
 */
	public void addMapping(String prefix, String expansion) {
		
		// return null;
	}
	
	public CoreAbbreviator clone() {
		CoreAbbreviator c = null;
		HashMap<String,String> clonedMap = new HashMap<String,String>(myPrefixMap);
		c = new CoreAbbreviator(clonedMap, myShortFormSeparator, myLongFormSeparator);
		return c;
	}
	
	public static CoreAbbreviator makeCoreAbbreviator(String prefix, String expansion, String shortFormSep, String longFormSep) {
		CoreAbbreviator result;
		HashMap<String,String> prefixMap = new HashMap<String,String>();
		prefixMap.put(prefix, expansion);
		result = new CoreAbbreviator(prefixMap, shortFormSep, longFormSep);
		return result;
	}

}
