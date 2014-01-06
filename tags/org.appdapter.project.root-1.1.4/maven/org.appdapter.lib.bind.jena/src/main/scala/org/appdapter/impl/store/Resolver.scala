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

package org.appdapter.impl.store

import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode}
import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}

import org.appdapter.core.name.{Ident, FreeIdent};


/**
 * @author Stu B. <www.texpedient.com>
 */



/**
 * Binding for an input column of URIs / QNames.   optDefPrefix may be an abbrev OR a URI prefix ;  If it contains
 * no colons, one is appended, making the input effectively, e.g. "xyz" -> "xyz:"
 * 
 * Ante ==> the prefix occurs before prefix-mapping resolution.
 */

class ResourceResolver(val myPrefixMap: PrefixMapping, val myOptDefAntePrefixWithOptColon : Option[String]) {

	val myPossDefAntePrefix : String = myOptDefAntePrefixWithOptColon match {
		case Some(defPrefix:String) => if (defPrefix.contains(":")) defPrefix else defPrefix + ":";
		case None => "";
	}

	def resolveURI(qnameOrURI:String) : String = {
		val qnOrURI : String = if (qnameOrURI.contains(":")) qnameOrURI else {myPossDefAntePrefix + qnameOrURI};
		val resolvedURI = myPrefixMap.expandPrefix(qnOrURI);
		resolvedURI;
	}
	
	def findOrMakeResource(model:Model, qnameOrURI:String) : Resource = {
		val uri = resolveURI(qnameOrURI);
		val res = model.createResource(uri);
		res;
	}
	// Jena low level API does not treat property and resource quite symmetrically.
	def findOrMakeProperty(model:Model, qnameOrURI:String) : Property = {
		val propRes = findOrMakeResource(model, qnameOrURI);
		val prop = propRes.as(classOf[Property]);
		prop;
	}
	
	def resolveIdent(qnameOrURI:String) : Ident = {
		val uri : String = resolveURI(qnameOrURI);
		// Todo:  grab the "end" of the input to use as "localName".
		val ident = new FreeIdent(uri, qnameOrURI);
		ident
	}
	
}
object ResourceResolver {

}
