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

package org.appdapter.core.store;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.RDFNode;
/**
 * @author Stu B. <www.texpedient.com>
 */

public interface InitialBinding {
	public QuerySolutionMap getQSMap();
	
	public void bindNode( String vName, RDFNode  node  );
	
	public void  bindQName(String vName, String resQName );
	public void  bindURI(String vName , String resURI);
	public void  bindIdent(String vName , Ident id);
	public void  bindLiteralString(String vName , String litString);
}
