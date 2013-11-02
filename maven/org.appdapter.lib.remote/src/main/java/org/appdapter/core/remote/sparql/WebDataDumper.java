/*
 *  Copyright 2013 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.remote.sparql;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class WebDataDumper {

	public static void dumpRequestInfo(HttpPost postReq, Logger log) {
		log.info("Request method: " + postReq.getMethod());
		log.info("Request line: " + postReq.getRequestLine());
		Header[] allHeaders = postReq.getAllHeaders();
		log.info("POST header count: " + allHeaders.length);
		for (Header h : allHeaders) {
			log.info("Header: " + h);
		}
	}

	public static void dumpResponseInfo(HttpResponse response, String rqSummary, String entityText, Logger log) throws Throwable {
		log.info("Request Summary: " + rqSummary);

		if (response != null) {
			log.info("Response status line: " + response.getStatusLine());
		} else {
			log.warn("Got null response to request: " + rqSummary);
		}
		if (entityText != null) {
			log.info("Entity Text: " + entityText);
		}
	}
}
