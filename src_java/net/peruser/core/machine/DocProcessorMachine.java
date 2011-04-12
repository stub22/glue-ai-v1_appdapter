package net.peruser.core.machine;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.peruser.core.command.Command;
import net.peruser.core.command.AbstractCommand;

import net.peruser.core.document.Doc;
import net.peruser.core.document.DocFactory;

import net.peruser.core.config.Config;
import net.peruser.core.config.MutableConfig;

import net.peruser.core.name.Address;

import net.peruser.core.environment.Environment;

// import static net.peruser.core.vocabulary.SubstrateAddressConstants.instructionAddress;
// import static net.peruser.core.vocabulary.SubstrateAddressConstants.opConfigRefPropAddress;

// BAD to import bindings in core!

import net.peruser.binding.dom4j.Dom4jDoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Skeletal implementation of a processing queue.<br/>
 * CommandMachine is a Machine that processes Commands in sequence.<br/> 
 * Calling the machine-level "process" method results in a new command being 
 * instantiated, scheduled and then executed. 
 * <p>Past commands are stored in a history list.</p>
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public abstract class DocProcessorMachine extends ProcessorMachine {
	
	private static Log 		theLog = LogFactory.getLog(DocProcessorMachine.class);	

	public Object process(Address instructAddr, Object input) throws Throwable {
		theLog.info(" process() input: " + input);
		Object output = null;
		
		Doc inputDoc = DocFactory.makeDocFromObject(input, true);
		
		Doc outputDoc = processDoc(instructAddr, (Doc) input);
		output = outputDoc;
		//	org.w3c.dom.Document outDocW3C = outputDoc.getW3CDOM();
		//	output = outDocW3C;
		theLog.info(" process() output: " + output);
		return output;
	}
	
	protected abstract Doc processDoc(Address instructAddr, Doc inputDoc) throws Throwable;
	
	
}		
