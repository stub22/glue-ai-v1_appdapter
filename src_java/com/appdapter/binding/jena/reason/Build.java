/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.appdapter.binding.jena.reason;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import java.util.Iterator;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class Build {

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
			// theLog.debug("Made inferred default model for dataset: " + infDefModel);
		}

		Iterator dni = underlyingDataset.listNames();

		while (dni.hasNext()) {
			String name = (String) dni.next();
			Model underModel = underlyingDataset.getNamedModel(name);
			Model infModel = makeInferredModel(underModel, reasoner);
			inferredDataset.addNamedModel(name, infModel);
			// theLog.debug("dataset inference[[name],[under],[inf]] = [[" + name + "],[" + underModel + "],[" + infModel + "]]");
		}

		return inferredDataset;
	}
}
