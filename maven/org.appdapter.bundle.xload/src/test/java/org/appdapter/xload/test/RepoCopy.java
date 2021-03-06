package org.appdapter.xload.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appdapter.bind.log4j.Log4jFuncs;
import org.appdapter.xload.rspec.URLRepoSpec;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.RepoOper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

public class RepoCopy {

	public static void main(String[] paths) {
		Log4jFuncs.forceLog4jConfig(RepoCopy.class.getResource("mylog4j.properties"));
		if (paths.length < 1) {
			copy("goog://0AsAJ7pzOB_F2dGVkcUlTSkJ1ZXhzUFVIckhxN25tQkE/9/8, goog://0AsAJ7pzOB_F2dDZYMUw0bHZSM1BZZTRYaFI5dGtTRGc/1/6", "./saveme/");
			copy("./saveme/dir.ttl", "roundtrip/");
			return;
		}
		copy(paths[0], paths[1]);
	}

	public static boolean copy(String path, String out) {
		List<ClassLoader> loaders = Arrays.asList(ClassLoader.getSystemClassLoader());
		URLRepoSpec repo = new URLRepoSpec(path, loaders);
		writeRepo(repo.makeRepo(), out);
		return true;
	}

	public static void writeRepo(Repo repo, String dir) {
		if (!(repo instanceof Repo.WithDirectory)) {
			return;
		}
		Dataset ds = repo.getMainQueryDataset();
		Model dirModel = ((Repo.WithDirectory) repo).getDirectoryModel();
		String csiURI = dirModel.getNsPrefixURI("csi");
		String rname = new java.text.SimpleDateFormat("yyyyMMddHH_mmss_SSS").format(new Date());
		Node fileRepoName = dirModel.getResource(csiURI + "filerepo_" + rname).asNode();
		Map<String, String> nsUsed = new HashMap<String, String>();
		try {
			RepoOper.saveRepoAsManyTTLs(fileRepoName, dir, nsUsed, dirModel, ds, false, false);
			// RepoOper.saveRepoAsManyTTLs(fileRepoName, dir, dirModel, ds,
			// false);
		} catch (IOException ex) {
			//
		}
	}
}