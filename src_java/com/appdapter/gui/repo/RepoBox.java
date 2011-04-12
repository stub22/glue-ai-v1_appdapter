/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.repo;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.Trigger;
import com.hp.hpl.jena.sdb.Store;
import java.util.List;

/**
 *
 * @author winston
 */
public interface RepoBox<TT extends Trigger<? extends RepoBox<TT>>> extends Box<TT> {
	public Store				getStore();

	public List<GraphStat>		getGraphStats();

	public String				getUploadHomePath();
	
	public static class GraphStat {
		String		graphURI;
		long		statementCount;
	}
}
