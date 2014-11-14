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

package org.appdapter.core.model;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This pivotal class plays a large and slightly ambiguous role.
 * 
 * It is assumed that this translator can do a "good" job of 
 * going from each input form to each output form.  In general,
 * this works out to be practically equivalent to the idea that 
 * it has access to:
 * 
 *    1) What we consider to be a "good" set of namespace prefix bindings, required for the qName mappings.
 *
 *	  2) What we consider to be a "good" way to construct Jena nodes of these kinds:
 *			a) resources - often presumed to be connectable in some way with a presumed relevant model.
 *			The ambiguity of this part is inherited by the common interpretaions of #3.a and #3.b
 *			b) literals, which traditionally we have constructed using Model, but there must be a plain factory, right?
 * 
 *    3) What we consider to be a "good" way to construct Appdapter URI wrappers of these kinds:
 *			a) Idents - which in this context are often assumed to be Items, JRIs in fact, wrapping #2.a above.
 *			b) Items - often assumed to be JRIs, wrapping #2a above.
 * 
 * All 3 of these capabilities become essentially natural if we are able to assume a particular
 * model as context, and thus prior versions of this functionality were grouped under the name
 * "ModelClient".   However, much (but not all) of the function of these features is rather different than
 * the idea of a "handle" to some model, which comes with its own conceptual baggage, pro and con.
 * 
 * Next note that we currently assume that a RepoClient can produce a "default" RdfNodeTranslator,
 * presumably of some "good" nature!  In the past this was translated into the assumption that a
 * handle to a presumed dir-Model in a connected local repo was available.  That assumption was
 * manifest by having RepoClient extend RdfNodeTranslator (then called ModelClient), which further
 * blurred the issue.   We backed off from there to say a RepoClient should just be able to return
 * a RdfNodeTranslator, rather than itself "be" one.
 */

public interface RdfNodeTranslator {
	
	public Resource makeResourceForURI(String uri);
	
	public Resource makeResourceForQName(String qName );
	
	public Resource makeResourceForIdent( Ident id  );
	
	public Literal makeTypedLiteral(String litString, RDFDatatype dtype );
	
	public Literal makeStringLiteral(String litString);
	
	public Ident makeIdentForQName(String qName);
	
	public Item makeItemForQName(String qName );
	
	public Ident makeIdentForURI(String uri);
	
	public Item makeItemForIdent(Ident id);
}
