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
import java.io.InputStream;

import java.net.URL;

import java.util.Iterator;
import java.util.Map;


import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

// See note below under loadJenaModelUsingJenaFileManager()
// import com.hp.hpl.jena.sparql.util.RelURI;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import org.appdapter.core.name.Ident;
import org.appdapter.core.item.Item;
/**
 * @author Stu B. <www.texpedient.com>
 */

public interface ModelClient {
	
	public Resource makeResourceForURI(String uri);
	
	public Resource makeResourceForQName(String qName );
	
	public Resource makeResourceForIdent( Ident id  );
	
	public Literal makeTypedLiteral(String litString, RDFDatatype dtype );
	
	public Literal makeStringLiteral(String litString);
	
	public Ident makeIdentForQName(String qName);
	
	public Item makeItemForQName(String qName );
	
	public Ident makeIdentForURI(String uri);
	
}
