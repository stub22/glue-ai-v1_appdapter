/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

	public static void dumpResponseInfo(HttpResponse response, String rqSummary, String entityText, Logger log)
			throws Throwable {
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
