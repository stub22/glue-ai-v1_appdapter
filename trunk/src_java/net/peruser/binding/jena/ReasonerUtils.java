package net.peruser.binding.jena;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.DataSource;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.reasoner.Reasoner;

import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import com.hp.hpl.jena.util.PrintUtil;

import net.peruser.core.config.Config;

import net.peruser.core.name.Address;

import net.peruser.core.vocabulary.SubstrateAddressConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Static convenience methods for rule-oriented processing.
 * <br/>
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */

public class ReasonerUtils implements SubstrateAddressConstants {
	private static Log 		theLog = LogFactory.getLog(ReasonerUtils.class );
	
	static {
		PrintUtil.registerPrefix("lut", "http://www.peruser.net/substrate#");
	}
	
	public static InfModel createInferenceModelUsingGenericRules (Model baseModel, String ruleSet) {
		//Resource rconf = ModelFactory.createDefaultModel().createResource();
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(ruleSet));
		// reasoner.setDerivationLogging(true);
		InfModel infModel = ModelFactory.createInfModel(reasoner, baseModel);
		return infModel;
	}
	
	/**
	 * Returns a java.util.List of com.hp.hpl.jena.reasoner.rulesys.Rule objects.
	 */
	
	
	public static List parseRulesListAtURL(String url) throws Throwable {
		return Rule.rulesFromURL(url);
	}
  	/**
		* These rules are passed to the Jena Reasoner (see Jena docs).  
		* These implication rules cause additional virutal triples to appear in the model.
		* Backward-recursive rules need to be "tabled" to prevent explosion.  (Makes it more similar to a forward rule).
		*/	
	public static Reasoner buildReasonerFromRulesAtURL (String url) throws Throwable {
		List ruleList = parseRulesListAtURL(url);
		Reasoner reasoner = new GenericRuleReasoner(ruleList);
		return reasoner;
	}
	
	public static InfModel makeInferredModel (Model underModel, Reasoner reasoner) {
		InfModel infModel = ModelFactory.createInfModel(reasoner, underModel);
		return infModel;
	}
	
	public static Dataset makeInferredDataset (Dataset underlyingDataset, Reasoner reasoner) {
		DataSource inferredDataset = DatasetFactory.create();
		Model defModel = underlyingDataset.getDefaultModel();
		if (defModel != null) {
			Model infDefModel = makeInferredModel(defModel, reasoner);
			inferredDataset.setDefaultModel(infDefModel);
			theLog.debug("Made inferred default model for dataset: " + infDefModel);
		}
		
		Iterator dni = underlyingDataset.listNames();
		
		while (dni.hasNext()) {
			String name = (String) dni.next();
			Model underModel = underlyingDataset.getNamedModel(name);
			Model infModel = makeInferredModel(underModel, reasoner);
			inferredDataset.addNamedModel(name, infModel);
			theLog.debug("dataset inference[[name],[under],[inf]] = [[" + name + "],[" + underModel + "],[" + infModel + "]]");
		}
		
		return inferredDataset;
	}
	static public OntModelSpec lookupOntModelSpec (Config conf, Address configAddress) throws Throwable {
		OntModelSpec result = null;
		if (configAddress != null) {
			Address inferenceMarkerAddress = conf.getSingleAddress(configAddress, inferenceMarkerPropAddress);
			theLog.debug("inferenceMarkerAddress is " + inferenceMarkerAddress);
			theLog.debug("canonical jenaRdfsInferenceMarkerAddress  is " + jenaRdfsInferenceMarkerAddress);
			theLog.debug("canonical noInferenceMarkerAddress  is " + noInferenceMarkerAddress);
			if (inferenceMarkerAddress.equals(jenaRdfsInferenceMarkerAddress)) {
				result = OntModelSpec.RDFS_MEM_RDFS_INF;
			} else if (inferenceMarkerAddress.equals(noInferenceMarkerAddress)) {
				result = null;
			} else {
				throw new Throwable ("Unknown inference type: " + inferenceMarkerAddress);
			}
		}
		return result;
	}			
}
