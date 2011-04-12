/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appdapter.core.store;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 */
public class AppRepo {

	static Logger theLogger = LoggerFactory.getLogger(AppRepo.class);
	private String myStoreConfigPath;
	private Store myStore;

	private AppRepo(String storeConfigPath) {
		myStoreConfigPath = storeConfigPath;
	}

	private void open() {
		theLogger.info("Connecting to store using config from: " + myStoreConfigPath);
		Store store = SDBFactory.connectStore(myStoreConfigPath);
		myStore = store;
	}

	public static AppRepo openRepo(String storeConfigPath) {
		AppRepo repo = null;
		return repo;
	}

	public void formatIfNeeded() {
		try {
			boolean isFormatted = StoreUtils.isFormatted(myStore);

			theLogger.info("isFormatted returned  " + isFormatted);
			if (!isFormatted) {
				theLogger.info("Creating SDB tables in store: " + myStore);
				myStore.getTableFormatter().create();
			} else {
				theLogger.warn("Store " + myStore + " is already formatted, so ignoring init command.");
			}
		} catch (Throwable t) {
			theLogger.error("problem in formatIfNeeded", t);
		}
	}
	/*
	protected ResultSet execQueryFromURL(String queryURL) {
			try {
				Query parsedQuery = QueryFactory.read(queryURL); //   create(queryText);
				Dataset ds = DatasetStore.create(myStore);
				QueryExecution qe = QueryExecutionFactory.create(parsedQuery, ds);
				try {
					ResultSet rs = qe.execSelect();
					ResultSetRewindable rsr = ResultSetFactory.makeRewindable(rs);
					ResultSetFormatter.out(rsr);
					System.out.println("and now how about XML\n===================================");
					rsr.reset();
					String resultXML = ResultSetFormatter.asXMLString(rsr);
					System.out.println(resultXML);

				} finally {
					qe.close();
				}
			} catch (Throwable t) {
				theLogger.error("problem in QueryTrigger", t);
			}
	}
	public
	 * 
	 */
}
