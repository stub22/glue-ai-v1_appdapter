/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.rdf.jena.query;
import com.hp.hpl.jena.query.ResultSet;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Defines a synchronous pattern for producing a particular ResType from 
 * a query result set.   This interface is a primary extension point.
 * 
 * By requiring client code to pass in these processors, we give it
 * flexibility, while ensuring our ARQ query will complete and release all 
 * resources, regardless of errors.
 * 
 */

public interface JenaArqResultSetProcessor<ResType> {
	public ResType processResultSet(ResultSet rset);
}
