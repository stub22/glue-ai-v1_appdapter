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

import java.util.List;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.store.Repo;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface RepoBox<TT extends Trigger<? extends RepoBox<TT>>> extends Box<TT> {
	public Repo getRepo();  // Return type should be yet another type parameter of the interface.
	
	public List<Repo.GraphStat>		getAllGraphStats();
	
	// This method does not really belong here, but it is useful during testing.
	public String processQueryAtUrlAndProduceXml(String queryURL, ClassLoader optResourceCL);
}
