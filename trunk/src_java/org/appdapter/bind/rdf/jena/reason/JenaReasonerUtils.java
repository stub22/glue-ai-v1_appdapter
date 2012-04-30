/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.rdf.jena.reason;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class JenaReasonerUtils {
	/*
	 * http://jena.sourceforge.net/inference/#rules
	 * Rule files may be loaded and parsed using:

List rules = Rule.rulesFromURL("file:myfile.rules");
or
BufferedReader br = // openReader
List rules = Rule.parseRules( Rule.rulesParserFromReader(br) );
or
String ruleSrc = // list of rules in line 
List rules = Rule.parseRules( rulesSrc );
In the first two cases (reading from a URL or a BufferedReader) the rule file is preprocessed by a simple 
		processor which strips comments and supports some additional macro commands:
# ...
A comment line.
// ...
A comment line.

@prefix pre: <http://domain/url#>.
Defines a prefix pre which can be used in the rules. The prefix is local to the rule file.

@include <urlToRuleFile>.
Includes the rules defined in the given file in this file. The included rules will appear before the user defined rules, irrespective of where in the file the @include directive appears. A set of special cases is supported to allow a rule file to include the predefined rules for RDFS and OWL - in place of a real URL for a rule file use one of the keywords RDFS OWL OWLMicro OWLMini (case insensitive).

	 */

	/**
	 * In this form, the ruleSet does not process @prefix or @include commands.
	 * @param baseModel
	 * @param ruleSet
	 * @return 
	 */
	public static InfModel createInferenceModelUsingGenericRules (Model baseModel, String ruleSet) {
		//Resource rconf = ModelFactory.createDefaultModel().createResource();
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(ruleSet));
		// reasoner.setDerivationLogging(true);
		InfModel infModel = ModelFactory.createInfModel(reasoner, baseModel);
		return infModel;
	}
	public static InfModel createInferenceModelUsingGenericRulesWithMacros (Model baseModel, String ruleSetWithMacros) {
		StringReader ruleStringReader = new StringReader(ruleSetWithMacros);
		BufferedReader ruleBufferedReader = new BufferedReader(ruleStringReader);
		Rule.Parser ruleParser = Rule.rulesParserFromReader(ruleBufferedReader);
		List<Rule>	ruleList = Rule.parseRules(ruleParser);
		Reasoner reasoner = new GenericRuleReasoner(ruleList);
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
