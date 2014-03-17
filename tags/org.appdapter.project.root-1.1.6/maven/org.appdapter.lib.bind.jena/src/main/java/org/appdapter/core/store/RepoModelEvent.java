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
package org.appdapter.core.store;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

public class RepoModelEvent {

	final public static String REPO_EVENT_NAMESPACE = "ccrt:";
	final public static String repoKey = "repo";
	final public static String sheetName = "modelName";
	final public static String loadStatus = "loadStatus";
	final public static String timestamp = "timeStampMS";
	final public static String pending = "Pending", loading = "Loading", loaded = "Loaded", unloading = "Unloading", unloaded = "Unloaded", cancelling = "Cancelling", cancelled = "Cancelled",
			error = "Error";

	static public void createEvent(Model saveEventsTo2, Map eventProps) {
		/*Resource eventName = saveEventsTo2.createResource();
		for (Map.Entry e : eventProps.entrySet()) {

		}*/
	}
}
