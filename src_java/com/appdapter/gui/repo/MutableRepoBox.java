/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.repo;

import com.appdapter.gui.box.Trigger;
import com.hp.hpl.jena.sdb.Store;

/**
 *
 * @author winston
 */
public interface MutableRepoBox<TT extends Trigger<? extends RepoBox<TT>>>  extends RepoBox<TT>  {
	public void setStore(Store store);
	public void mountStoreUsingFileConfig(String storeConfigPath);
	public void formatStoreIfNeeded();

	public void uploadModelFile(String fileName, String graphName, boolean replace);
}
