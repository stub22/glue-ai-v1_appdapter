/*
 *  Copyright 2014 by The Friendularity Project (www.friendularity.org).
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

package org.appdapter.fancy.gportal

import com.hp.hpl.jena.rdf.model.{ Model, Resource, Literal }
import com.hp.hpl.jena.query.{DatasetAccessor, DatasetAccessorFactory}
import com.hp.hpl.jena.query.{ Dataset, DatasetFactory, ReadWrite }

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper

import org.appdapter.fancy.log.VarargsLogging

/**
 * @author Stu B. <www.texpedient.com>
 */
case class SuppliedGraphStat(myAbsUriTxt : String, myLabel : String, myCount : Long) {
}
trait SGS_Mapper extends Function1[SuppliedGraphStat, Option[String]]
trait SGS_Filter extends Function1[SuppliedGraphStat, Boolean]

trait  GraphSupplier extends GraphPortal {
	// These options are possible with both remote and local graph hosts.
	def getNamedGraph_Readonly(graphURI : String) : Model = null
	protected def getNamedGraph_Naive(graphURI : String) : Model = null
	protected def fetchStats_Naive() : List[SuppliedGraphStat] = Nil
	
	def fetchStats(optFilter : Option[SGS_Filter]) : List[SuppliedGraphStat] = {
		fetchStats_ReadTxCompat(optFilter)
	}
	protected def fetchStats_ReadTxCompat(optFilter : Option[SGS_Filter]) : List[SuppliedGraphStat] = {
		val oper = new Oper[List[SuppliedGraphStat]]() {
			override def perform(): List[SuppliedGraphStat] = {
				val allStats = fetchStats_Naive()
				if (optFilter.isDefined) {
					val filterFunc = optFilter.get
					allStats.filter(filterFunc)
				} else {
					allStats
				}
			}
		}
		execReadTransCompatible(oper, Nil)
	}
	
}


trait DsaccGraphSupplier extends GraphSupplier with DsaccGraphPortal {
	// def getDatasetAccessor : DatasetAccessor
	override protected def getNamedGraph_Naive(graphURI : String) : Model = {
		getDatasetAccessor.getModel(graphURI)
	}
	protected def getNamedGraph_ReadTransCompatible(graphURI : String) : Model = {
		val op = new Oper[Model] {
			override def perform(): Model = {
				getNamedGraph_Naive(graphURI)
			}
		}
		execReadTransCompatible(op, null)
	}
	override def getNamedGraph_Readonly(graphURI : String) : Model = getNamedGraph_ReadTransCompatible(graphURI)
}

trait LocalGraphSupplier // (private val myDataset : Dataset) 
			extends DsaccGraphSupplier with LocalGraphPortal {
//	lazy val myDacc : DatasetAccessor = DatasetAccessorFactory.create(myDataset)
//	override def getDatasetAccessor = myDacc
	override protected def fetchStats_Naive() : List[SuppliedGraphStat] = {
		val vl : VarargsLogging = getVarargsLogger
		val dset : Dataset = getLocalDataset
		var numModels : Int = 0
		var stats : List[SuppliedGraphStat] = Nil
		import scala.collection.JavaConversions._
		dset.listNames foreach (graphName => {
			val m = dset.getNamedModel(graphName)
			numModels = numModels + 1
			vl.debug3("LocalGraphSupplier.fetchStats_Naive found {}-th model at {} containing {} statements", numModels : java.lang.Integer, graphName, m.size : java.lang.Long)
			val stat = new SuppliedGraphStat(graphName, graphName, m.size())
			stats = stats :+ stat
		})			
		vl.debug1("Found {} models in total", numModels : java.lang.Integer)
		stats
	}	
		
}

trait RemoteGraphSupplier extends DsaccGraphSupplier with RemoteGraphPortal {
	
	val myGraphNameQueryText = "SELECT DISTINCT ?g WHERE { GRAPH ?g  {?s ?p ?o}}";
	
	override protected def fetchStats_Naive() : List[SuppliedGraphStat] = {
		val vl : VarargsLogging = getVarargsLogger
		var numModels : Int = 0
		var stats : List[SuppliedGraphStat] = Nil
		
		val remoteQueryURL = getRemoteQueryServiceURL
		
		//  DatasetAccessor interface does not directly support "list named graphs", so instead we use a SPARQL query.
		vl.debug1("Found {} models in total", numModels : java.lang.Integer)
		stats		
	}
}
