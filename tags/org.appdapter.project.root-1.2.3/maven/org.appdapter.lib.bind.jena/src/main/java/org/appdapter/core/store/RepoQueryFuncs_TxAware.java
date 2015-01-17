/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware;
import com.hp.hpl.jena.query.Dataset;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class RepoQueryFuncs_TxAware {
	public static <RetType> RetType execReadTransCompatible(Repo r, RetType onFailure, JenaArqQueryFuncs_TxAware.Oper<RetType> oper) {
		Dataset dset = (r != null) ? (r.getMainQueryDataset()) : null;
		return JenaArqQueryFuncs_TxAware.execReadTransCompatible(dset, onFailure, oper);
	}
	public static <RetType> RetType execWriteTransCompatible(Repo r, RetType onFailure, JenaArqQueryFuncs_TxAware.Oper<RetType> oper) {
		Dataset dset = (r != null) ? (r.getMainQueryDataset()) : null;
		return JenaArqQueryFuncs_TxAware.execWriteTransCompatible(dset, onFailure, oper);
	}
	
}
