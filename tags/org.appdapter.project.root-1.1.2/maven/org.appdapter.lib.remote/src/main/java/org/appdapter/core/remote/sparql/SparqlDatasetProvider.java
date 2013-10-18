package org.appdapter.core.remote.sparql;

import org.appdapter.core.store.RepoOper;
import org.appdapter.core.store.dataset.JenaSDBWrappedDatasetFactory;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;
import org.appdapter.demo.DemoResources;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;

public class SparqlDatasetProvider extends JenaSDBWrappedDatasetFactory implements UserDatasetFactory {

	static SparqlDatasetProvider SINGLETON = new SparqlDatasetProvider();

	static void install() {
		RepoDatasetFactory.registerDatasetFactory("default", SINGLETON);
		RepoOper.registerDatasetFactory("remote", SINGLETON);
		RepoOper.registerDatasetFactory("shared", SINGLETON);
	}

	@Override public Dataset createDefault() {
		if (true)
			return createRemotePeer();
		return create(createMem());
	}

	@Override public Dataset createMem() {
		return create(createMem());
	}

	@Override public Dataset create(Dataset peer) {
		Dataset remote = createRemotePeer();
		RepoDatasetFactory.addDatasetSync(peer, remote);
		return peer;
	}

	@Override public Dataset createRemotePeer() {
		Store store = SDBFactory.connectStore(DemoResources.STORE_CONFIG_PATH);
		Dataset ds = SDBFactory.connectDataset(store);
		return ds;
	}

}
