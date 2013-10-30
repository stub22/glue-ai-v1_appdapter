package org.appdapter.core.remote.sparql;

import org.appdapter.core.matdat.OmniLoaderRepo;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

public class SparqlRepo extends OmniLoaderRepo {

	private String endpointURI;
	private String dirModelName;

	public SparqlRepo(String endpointURI, String dirModelName) {
		super(null);
		this.endpointURI = endpointURI;
		this.dirModelName = dirModelName;
	}

	@Override public Model getDirectoryModel() {
		return getMainQueryDataset().getNamedModel(this.dirModelName);
	}

	@Override protected Dataset makeMainQueryDataset() {
		return new SparqlDataset(new SparqlDatasetGraph(endpointURI));
	}
}
