package org.appdapter.core.remote.sparql;

import org.appdapter.core.matdat.OmniLoaderRepo;
import org.appdapter.help.repo.RepoClient;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

public class RepoFromClient extends OmniLoaderRepo {

	private RepoClient rc;
	private String dirModelName;

	public RepoFromClient(RepoClient rc, String dirModelName) {
		super(null);
		this.rc = rc;
		this.dirModelName = dirModelName;
	}

	@Override public Model getDirectoryModel() {
		return getMainQueryDataset().getNamedModel(this.dirModelName);
	}

	@Override protected Dataset makeMainQueryDataset() {
		return rc.getRepo().getMainQueryDataset();
	}

	@Override public Dataset getMainQueryDataset() {
		return rc.getRepo().getMainQueryDataset();
	}
}
