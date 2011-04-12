package net.peruser.core.config;

import java.util.ArrayList;
import java.util.List;

import net.peruser.core.document.Doc;
import net.peruser.core.document.SentenceValue;

import net.peruser.binding.dom4j.Dom4jDoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.peruser.core.name.Address;
import net.peruser.core.name.Abbreviator;

import net.peruser.core.process.Data;

import net.peruser.core.environment.Environment;

/**
 * A DataConfig is based on processor.Data
 * <br/>
 * Information in the model is contrued as "Frames" and "Slots".
 * <br/>
 * This class does not know what kind of Model it is using, or where it comes from.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class DataConfig extends AbstractConfig {
	private static Log 		theLog = LogFactory.getLog(DataConfig.class);
	
	private		Data				myData;
	private		Abbreviator			myAbbreviator;
	

	public List getFieldValues (Address thingAddress, Address fieldAddress) throws Throwable {

		//Model actMod = getActiveJenaModel();
		List result = new ArrayList();
		/*
		Resource frameResource = resolveFrame(thingAddress);
		Property slotProperty = resolveSlot(fieldAddress);		
		theLog.debug("Fetching values for slot " + slotProperty + " in frame " + frameResource);		
		StmtIterator	matchIter = actMod.listStatements(frameResource, slotProperty, (RDFNode) null);
		while (matchIter.hasNext()) {
			Statement statement = (Statement) matchIter.next();
			RDFNode valNode = statement.getObject();
			theLog.debug("FOUND: " + valNode);
			if (valNode instanceof Resource) {
				Address valAddress = new JenaAddress((Resource) valNode);
				result.add(valAddress);
			} else {
				Literal lit = (Literal) valNode.as(Literal.class);
				// Decision - convert this literal into a Doc or not?
				result.add(lit.getString());
			}
		}
		*/
		return result;
	}
	public List getBackpointerFieldValues (Address thingAddress, Address fieldAddress) throws Throwable {
		// Model actMod = getActiveJenaModel();
		List result = new ArrayList();
		/*
		StmtIterator	matchIter = actMod.listStatements(null, resolveSlot(fieldAddress), resolveFrame(thingAddress));
		while (matchIter.hasNext()) {
			Statement statement = (Statement) matchIter.next();
			Address valAddress = new JenaAddress(statement.getSubject());
			result.add(valAddress);
		}
		*/
		return result;
	}
	/*
	public void applyOverrides (Doc d) throws Throwable {
		// CoreAbbreviator abb = CoreAbbreviator.makeCoreAbbreviator("WRONG", "NOPE", null, null);
		// Using "myAbbreviator" means that prefixes in the doc must be same as in our backing model.
		d.applyOverrides (this, myAbbreviator);
	}
	
	public void clearValues(Address thing, Address field) throws Throwable {
	}
	public void addAddressValuedSentence(Address thingAddress, Address fieldAddress, Address valueAddress) throws Throwable {
	}
	public void addStringValuedSentence(Address thingAddress, Address fieldAddress, String valueString) throws Throwable {
	}
	public void addDocValuedSentence(Address thingAddress, Address fieldAddress, Doc valueDoc) throws Throwable {
	} 
	*/
	public MutableConfig makeMutableCloneConfig(Environment env) throws Throwable {
		MutableConfig result = null;
		return result;
	}
}

