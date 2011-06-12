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

package com.appdapter.peru.binding.jena;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.query.QueryBuildException;

import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.QueryIterator;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;

// import com.hp.hpl.jena.sparql.pfunction.PFLib;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimpleAndList;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.util.IterLib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Example of an ARQ "Property Function". 
 *
 * See http://jena.sourceforge.net/ARQ/extension.html
 *
 * Expects an unbound variable as subject, and a list of values which are convertible to float as object.
 * Binds the subject variable to the sum of the object floats.
 */

public class sum extends PFuncSimpleAndList {
	
	private static Log 		theLog = LogFactory.getLog(sum.class);

	public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg object, 
				ExecutionContext execCxt) {
		QueryIterator 		result;
		List	inputList = object.getArgList();
		
		theLog.debug("Computing sum " + subject + " using input list " + inputList);
		Iterator lit = inputList.iterator();
		
		double sumval = 0.0;
		while (lit.hasNext()) {
			Node addendNode = (Node) lit.next();
			if (addendNode.isLiteral()) {
				Number addendValueNumber = (Number) addendNode.getLiteralValue();
				double addendValue = addendValueNumber.doubleValue();
				sumval += addendValue;
			} else {
				theLog.debug("Skipping non-literal (unbound?) value: " + addendNode);
			}
		}
		String	sumString = Double.toString(sumval);
				
		if (Var.isVar(subject)) 
        {
			Node resultValueNode = Node.createLiteral(sumString, null, XSDDatatype.XSDdouble);
			// It's unclear to me whether we're supposed to call Var.alloc or let the PFLib do this for us.
			// So far this seems to be working.
			Var resultVar = Var.alloc(subject);
			result = IterLib.oneResult(binding, resultVar, resultValueNode, execCxt);
		} else {
			throw new QueryBuildException("subject for sum predicate must be an unbound variable");
		}
		return result;
	}
}
