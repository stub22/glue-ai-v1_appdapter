/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.core.item;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author winston
 *
 * Keeps a copy of an ARQ select result, using memory, but
 * allowing us to close the result set and query.
 */
public class ArqRowItem extends ResultItem {
	private	int					myRowNumber;
	private	QuerySolutionMap	mySolutionCopy;
	public	ArqRowItem(int rowNumber, QuerySolution soln) {
		myRowNumber = rowNumber;
		// Must consume the soln.
		mySolutionCopy = new QuerySolutionMap();
		mySolutionCopy.addAll(soln);
	}
	@Override protected Literal getLiteralVal(Ident fieldID, boolean throwOnFailure) {
		String varName = fieldID.getLocalName();
		Literal lit = mySolutionCopy.getLiteral(varName);
		if (lit != null) {
			return lit;
		} else {
			throw new RuntimeException("Cannot locate literal value for varName: " + varName + " extracted from fieldID: " + fieldID);
		}
	}

	@Override protected List<Item> getLinkedItems(Ident linkName) {
		List<Item> result = new ArrayList<Item>();
		String varName = linkName.getLocalName();
		Resource r1 = mySolutionCopy.getResource(varName);
		if (r1 != null) {
			Item resultItem = new JenaResourceItem(r1);
			result.add(resultItem);
		}
		return result;
	}

	@Override public Ident getIdent() {
		throw new UnsupportedOperationException("ArqRowItem doesn't have an ident yet");
	}

}
