/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicRepoImpl extends BasicQueryProcessorImpl implements Repo {
	private Dataset myMainQueryDataset;

	public void replaceNamedModel(Ident modelID, Model jenaModel) {
		Dataset repoDset = getMainQueryDataset();
		DataSource repoDsource = (DataSource) repoDset;
		Lock lock = repoDsource.getLock();
		try {
			lock.enterCriticalSection(false);
			repoDsource.replaceNamedModel(modelID.getAbsUriString(), jenaModel);
		} finally {
			lock.leaveCriticalSection();
		}
	}

	// A bit like database's addNamedModel (but this is not implmentation of Mutable.. unless a subclass claims it is)
	public void addNamedModel(Ident modelID, Model jenaModel) {
		Dataset repoDset = getMainQueryDataset();
		DataSource repoDsource = (DataSource) repoDset;
		Lock lock = repoDsource.getLock();
		try {
			lock.enterCriticalSection(false);
			String name = modelID.getAbsUriString();
			if (!repoDsource.containsNamedModel(name)) {
				repoDsource.addNamedModel(name, jenaModel);
			} else {
				Model before = repoDsource.getNamedModel(name);
				jenaModel.add(before);
				repoDsource.replaceNamedModel(name, jenaModel);
			}
		} finally {
			lock.leaveCriticalSection();
		}
	}

	protected abstract Dataset makeMainQueryDataset();

	@Override public Dataset getMainQueryDataset() {
		if (myMainQueryDataset == null) {
			myMainQueryDataset = makeMainQueryDataset();
		}
		return myMainQueryDataset;
	}

	@Override public List<GraphStat> getGraphStats() {
		List<GraphStat> stats = new ArrayList<GraphStat>();
		Dataset mainDset = getMainQueryDataset();
		Iterator<String> nameIt = mainDset.listNames();
		while (nameIt.hasNext()) {
			String modelName = nameIt.next();
			Repo.GraphStat gs = new GraphStat();
			gs.graphURI = modelName;
			Model m = mainDset.getNamedModel(modelName);
			gs.statementCount = m.size();
			stats.add(gs);
		}
		return stats;
	}

	@Override public <ResType> ResType processQuery(Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc) {
		ResType result = null;
		try {
			Dataset ds = getMainQueryDataset();
			result = JenaArqQueryFuncs.processDatasetQuery(ds, parsedQuery, initBinding, resProc);
		} catch (Throwable t) {
			getLogger().error("problem in processQuery [{}]", parsedQuery, t);
		}
		return result;
	}

	@Override public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding) {
		Dataset ds = getMainQueryDataset();
		return JenaArqQueryFuncs.findAllSolutions(ds, parsedQuery, initBinding);
	}

	@Override public Model getNamedModel(Ident graphNameIdent) {
		Dataset mqd = getMainQueryDataset();
		String absURI = graphNameIdent.getAbsUriString();
		return mqd.getNamedModel(absURI);
	}

	@Override public Set<Object> assembleRootsFromNamedModel(Ident graphNameIdent) {
		Model loadedModel = getNamedModel(graphNameIdent);
		if (loadedModel == null) {
			getLogger().error("No model found at {}", graphNameIdent);
			// We *could* return an empty set, instead.
			return null;
		}
		Set<Object> results = AssemblerUtils.buildAllRootsInModel(loadedModel);
		return results;
	}
}
