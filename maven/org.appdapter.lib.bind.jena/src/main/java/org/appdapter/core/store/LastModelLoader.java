package org.appdapter.core.store;

import java.util.List;

import org.appdapter.core.repo.RepoSpec;
import org.appdapter.core.store.dataset.SpecialRepoLoader;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

public class LastModelLoader implements InstallableSpecReader,
		InstallableRepoLoader {
	public LastModelLoader() {
	}

	@Override
	public String getExt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RepoSpec makeRepoSpec(String s, String[] path, List<ClassLoader> cLs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDerivedLoader() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadModelsIntoTargetDataset(SpecialRepoLoader repo,
			Dataset mainDset, Model dirModel, List<ClassLoader> fileModelCLs) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getContainerType() {
		// TODO Auto-generated method stub
		return null;
	}
}