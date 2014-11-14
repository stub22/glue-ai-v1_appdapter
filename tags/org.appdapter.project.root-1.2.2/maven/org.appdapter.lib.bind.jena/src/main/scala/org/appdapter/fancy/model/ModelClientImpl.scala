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

package org.appdapter.fancy.model

import com.hp.hpl.jena.rdf.model.{Model, Resource, Literal}
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.datatypes.RDFDatatype
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import org.appdapter.core.name.Ident
import org.appdapter.core.item.{Item, JenaResourceItem}
import org.appdapter.bind.rdf.jena.model.SerialJenaResItem;
/**
 * @author Stu B. <www.texpedient.com>
 */


class ModelClientImpl (private val myModel : Model) extends ModelClientCore {
	def getModel : Model = myModel;
}
trait ModelClientCore extends org.appdapter.core.model.RdfNodeTranslator {
	protected def getModel : Model;
	
	override def makeResourceForURI(uri : String) : Resource = {
		getModel.createResource(uri)
	}
	override def makeResourceForQName(qName : String) : Resource = {
		val expandedURI = getModel.expandPrefix(qName)
		makeResourceForURI(expandedURI)
	}
	override def makeResourceForIdent(id : Ident) : Resource = {
		val uri : String = id.getAbsUriString
		makeResourceForURI(uri)
	}
	override def makeTypedLiteral(litString : String, dtype : RDFDatatype) : Literal = {
		getModel.createTypedLiteral(litString, dtype);
	}
	override def makeStringLiteral(litString : String) : Literal = {
		makeTypedLiteral(litString, XSDDatatype.XSDstring);
	}
	
	override def makeIdentForQName(qName : String) : Ident = makeJenaResourceItemForQName(qName)
	override def makeItemForQName(qName : String) : Item = makeJenaResourceItemForQName(qName)
	

	private def makeJenaResourceItemForQName(qName : String) : JenaResourceItem = {	
		val res = makeResourceForQName(qName)
		new SerialJenaResItem(res)
	}
	override  def makeIdentForURI(uri : String) : Ident = {
		val res = makeResourceForURI(uri)
		new SerialJenaResItem(res);
	}
	override  def makeItemForIdent(id : Ident) : Item = {
		id match {
			case itemAlready : Item  => itemAlready
			case otherIdent : Ident => { 
				val res = makeResourceForIdent (otherIdent); 
				new SerialJenaResItem(res)
			}
		}
	}
}
