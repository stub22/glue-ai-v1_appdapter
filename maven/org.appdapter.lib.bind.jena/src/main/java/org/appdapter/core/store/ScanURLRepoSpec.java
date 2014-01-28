package org.appdapter.core.store;

import java.util.List;

import org.appdapter.core.matdat.RepoSpec;
import org.appdapter.core.store.Repo.WithDirectory;

public class ScanURLRepoSpec extends RepoSpec {

	public ScanURLRepoSpec(String dirModelURL, List<ClassLoader> fileModelCLs) {

	}

	@Override public WithDirectory makeRepo() {
		return null;
	}

}
