package net.peruser.module.projector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.rdf.model.Model;

import net.peruser.binding.jena.JenaPulljector;
import net.peruser.binding.jena.ModelUtils;
import net.peruser.binding.jena.ReasonerUtils;

import net.peruser.core.command.DocCommand;

import net.peruser.core.config.Config;

import net.peruser.core.document.Doc;
import net.peruser.binding.dom4j.Dom4jDoc;

import net.peruser.core.environment.Environment;

import net.peruser.core.name.Address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;


import static net.peruser.module.projector.ProjectorAddressConstants.*;
/*
import static net.peruser.module.projector.ProjectorAddressConstants.rootPropAddress;
import static net.peruser.module.projector.ProjectorAddressConstants.identPropAddress;
*/

/**  ProjectorCommand currently relies directly on JenaPulljector, which violates our package
 *   dependency design constraint.
 *
 *  Many constants used here come from SubstrateAddressConstants.
 *  It seems they should come from...ProjectorAddressConstants.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ProjectorCommand extends DocCommand {
	private static Log 		theLog = LogFactory.getLog(ProjectorCommand.class);
	
	// These instance variables need to get set from the RDF config.
	// Currently, that's a lot of work.  Using some JDK 1.5 features 
	// (e.g. annotations or varargs) might help.  We could cast this
	// as a wrapper around SPARQL, or try to harness one of the RDF
	// path languages, which don't seem too impressive yet.  
	// Until we're ready to try that big cleanup, we're going to try 
	// to get by with just some weak java utilities built into Command
	// and Config.  
	
	// What model are we querying?
	private		String			myPlacePath;
	// What namespace is that model "bound to"? 
	private		String			myPlaceBaseURI;
	// What kind of inference are we using?
	private		OntModelSpec 	myOntModelSpec = null;
	
	// What resource should we start from?
	private		String		myRootQueryURI;
	// How deep should we go?
	private		int			myMaxDepth;
	
	// What properties should we follow as "links" (nested elements)?
	private		Set			myLinkQueries = new HashSet();
	// What properties should we dump out as "fields" (attributes)?
	private		List 		myFieldPropUris = new ArrayList();
	
	public Doc workDoc(Doc ignoredInputDoc) throws Throwable {
		Document rawResult = null;
		// Here we usedta blithely open a file. 
		// FileInputStream	fis = new FileInputStream(myPlacePath);
		// Create an object that knows how to return ProjectedNode cursors.
		// We should instead be getting this Projector from some kind of factory.
		// new equiv to old way:
		// loadJenaModelFromXmlSerialStream
		
		Model baseModel = ModelUtils.loadJenaModelUsingJenaFileManager(myEnvironment, myPlacePath);
		JenaPulljector jp = JenaPulljector.makePulljectorFromBaseModelAndOntSpec(baseModel, myOntModelSpec);

		// DEBUG: Dump the uri prefixes that jp knows about to stdout.
		// What's the easiest way to do this to the log stream?
		jp.dumpPrefixes(System.out);
		
		// Get a query cursor pointing into the model. 
		ProjectedNode pn = jp.projectNode(myRootQueryURI, myLinkQueries);
		
		// DEBUG: Traverse the tree found at pn, printing the given fields at each node to stdout
		// What's the easiest way to do this to the log stream?
		ProjectorUtils.printProjectedNodeTree(System.out, pn, myFieldPropUris);
		
		// Create an XML doc containing at most myMaxDepth levels.
		rawResult = ProjectorUtils.createDom4jDocument(pn, myFieldPropUris, myMaxDepth);
		return new Dom4jDoc(rawResult);
	}
	
	protected SimpleAxisQuery buildLinkQuery (Config opConf, Address linkConfigAddress) throws Throwable {
		Address linkRefAddress = opConf.getSingleAddress(linkConfigAddress, ProjectorAddressConstants.linkPropRefPropAddress);
		Address linkQueryAddress = resolveRef(linkRefAddress);
		
		theLog.debug("Link query address is " + linkQueryAddress); 
		
		Address linkMarkerAddress = opConf.getSingleAddress(linkConfigAddress, ProjectorAddressConstants.linkMarkerPropAddress);
		int queryType = -1;
		theLog.debug("linkMarkerAddress is " + linkMarkerAddress);
		theLog.debug("canonical forwardAddress is " + forwardMarkerAddress);
		if (linkMarkerAddress.equals(forwardMarkerAddress)) {
			queryType = SimpleAxisQuery.PARENT_POINTS_TO_CHILD;
		} else if (linkMarkerAddress.equals(reverseMarkerAddress)) {
			queryType = SimpleAxisQuery.CHILD_POINTS_TO_PARENT;
		}
		SimpleAxisQuery linkQuery = new SimpleAxisQuery(linkQueryAddress.getResolvedPath(), queryType);
		return linkQuery;
	}

	
	public void configure (Environment env, Config configImpl, Address configInstanceAddress)
				throws Throwable {
					
		super.configure(env, configImpl, configInstanceAddress);

		Config opConf = configImpl;
		
		Address commandConfig = configInstanceAddress;
		
		Address[]	spaceIdentPath = {rootPropAddress, spacePropAddress, identPropAddress};
		
		String maxDepthString = opConf.getSingleString(commandConfig, depthAddress);
		myMaxDepth = Integer.parseInt(maxDepthString);
		theLog.debug("maxDepth of op  is " + myMaxDepth);

		Address rootThing = opConf.getSingleAddress(commandConfig, rootPropAddress);
		theLog.debug("rootThingRef is " + rootThing);
		
		String rootThingIdent =  opConf.getSingleString (rootThing, identPropAddress);
		theLog.debug("rootThingIdent is " + rootThingIdent);
	
		Address rootThingSpace =  opConf.getSingleAddress (rootThing, spacePropAddress);
		theLog.debug("rootThingSpace is " + rootThingSpace);
		
		String spaceIdent = opConf.getSingleString(rootThingSpace, identPropAddress);
		theLog.debug("spaceIdent is " + spaceIdent);
		myPlaceBaseURI = spaceIdent;

		
		Address rootQueryAddress = resolveRef(rootThing);
		myRootQueryURI = rootQueryAddress.getResolvedPath();
		theLog.debug("rootQueryURI is " + myRootQueryURI);
		
		Address boundPlaceAddress = opConf.getSingleBackpointerAddress(rootThingSpace, boundSpacePropAddress);
		theLog.debug("boundPlace is " + boundPlaceAddress);
		
		// This is working for SelectorCommand IN THE ConsoleEnvironment, but ExcaliburEnvironment strips "file:"
		//myPlacePath = getMappedPlaceURL(boundPlaceAddress);
		myPlacePath = getRawPlaceURL(boundPlaceAddress);
		theLog.debug("placePath is " + myPlacePath);
		
		myOntModelSpec = ReasonerUtils.lookupOntModelSpec(opConf, commandConfig);

		// There could actually be multiple link configs 
		List linkConfigs = opConf.getFieldValues(commandConfig, linkConfigPropAddress);
		Iterator lcit = linkConfigs.iterator(); 
		while (lcit.hasNext()) {
			Address linkConfigAddress = (Address) lcit.next();
			SimpleAxisQuery linkQuery = buildLinkQuery(opConf, linkConfigAddress);
			myLinkQueries.add(linkQuery);
		}
		List fieldConfigs = opConf.getFieldValues(commandConfig, fieldConfigPropAddress);
		Iterator fcit = fieldConfigs.iterator(); 
		
		while (fcit.hasNext()) {
			Address fieldConfigAddress = (Address) fcit.next();
			
			Address fpra =  opConf.getSingleAddress(fieldConfigAddress, fieldPropRefPropAddress);
			Address fieldQueryAddress = resolveRef(fpra);
			String  fieldLabel = opConf.getSingleString (fieldConfigAddress, fieldLabelPropAddress);
			myFieldPropUris.add(fieldQueryAddress.getResolvedPath());
		}
	}

}
