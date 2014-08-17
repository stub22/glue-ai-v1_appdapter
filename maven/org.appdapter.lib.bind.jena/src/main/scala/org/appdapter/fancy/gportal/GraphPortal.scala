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
import org.appdapter.fancy.log.VarargsLogging

import org.appdapter.bind.rdf.jena.query.{JenaArqQueryFuncs_TxAware}
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Portal uses a monadic approach to transaction bracketing, and failure handling.
 */

trait GraphPortal {
	def getPortalLogger : Logger 
	def getVarargsLogger : VarargsLogging
	
	/**
	 * Default impl is not actually transaction aware, so it will work with a remote dataset.
	 */
	def  execReadTransCompatible[RetType](oper : Oper[RetType], onFailure : RetType) : RetType = {
		var result : RetType = onFailure
		try {
			result = oper.perform();
		} catch  {
			case ex : Throwable => {
				getPortalLogger.error("Caught error during READ op, aborting", ex);
			}
		}			
		result;
	}
	/**
	 * Default impl is not actually transaction aware, so it will work with a remote dataset.
	 */
	def  execWriteTransCompatible[RetType](oper : Oper[RetType], onFailure : RetType) : RetType = {
		var result : RetType = onFailure
		try {
			result = oper.perform();
		} catch  {
			case ex : Throwable => {
				getPortalLogger.error("Caught error during WRITE op, aborting", ex);
			}
		}			
		result;
	}
	
}
abstract class LoggingPortal extends VarargsLogging with GraphPortal {
	override def getPortalLogger : Logger = getLogger
	override def getVarargsLogger : VarargsLogging = this
}

trait  DsaccGraphPortal extends GraphPortal {
	def getDatasetAccessor : DatasetAccessor
}

trait LocalGraphPortal extends DsaccGraphPortal {
	protected def getLocalDataset : Dataset
	lazy val myDacc : DatasetAccessor = DatasetAccessorFactory.create(getLocalDataset)
	override def getDatasetAccessor = myDacc
	
	override def  execReadTransCompatible[RetType](oper : Oper[RetType], onFailure : RetType) : RetType = {
		val dset = getLocalDataset
		// Note that oper is *not* handed the dset - the dataset is only used for transaction bracketing
		JenaArqQueryFuncs_TxAware.execReadTransCompatible(dset, onFailure, oper)
	}
	override def  execWriteTransCompatible[RetType](oper : Oper[RetType], onFailure : RetType) : RetType = {
		val dset = getLocalDataset
		// Note that oper is *not* handed the dset - the dataset is only used for transaction bracketing
		JenaArqQueryFuncs_TxAware.execWriteTransCompatible(dset, onFailure, oper)
	}
}

trait RemoteGraphPortal extends DsaccGraphPortal {
	protected def getRemoteDataServiceURL : String
	lazy val myDacc : DatasetAccessor = DatasetAccessorFactory.createHTTP(getRemoteDataServiceURL);
	override def getDatasetAccessor = myDacc
	
	protected def getRemoteQueryServiceURL : String
	protected def getRemoteUpdateServiceURL : String
}

