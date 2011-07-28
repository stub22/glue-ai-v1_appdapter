/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.gui.repo;

import org.appdapter.gui.box.Box;
import org.appdapter.gui.box.Trigger;
import com.hp.hpl.jena.sdb.Store;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
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
