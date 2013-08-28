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
