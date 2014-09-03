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

package org.appdapter.fancy.gportal
import com.hp.hpl.jena.query.{ Dataset }

/**
 * @author Stu B. <www.texpedient.com>
 */

trait DelegatingPortal extends GraphPortal {
	def getSupplier : GraphSupplier 
	def getQuerier  : GraphQuerier
	def getAbsorber : GraphAbsorber 
	def getUpdater  : GraphUpdater
}

class LazyLocalDelegatingPortal(val myDataset : Dataset) extends LoggingPortal with LocalGraphPortal with DelegatingPortal  {
	
	override protected def getLocalDataset : Dataset = myDataset
	lazy val mySupplier = new LoggingPortal with LocalGraphSupplier {
		override protected def getLocalDataset : Dataset = myDataset
	}
	lazy val myQuerier = new LoggingPortal with LocalGraphQuerier {
		override protected def getLocalDataset : Dataset = myDataset
	}
	lazy val myAbsorber = new LoggingPortal with LocalGraphAbsorber {
		override protected def getLocalDataset : Dataset = myDataset
	}
	lazy val myUpdater = new LoggingPortal with LocalGraphUpdater {
		override protected def getLocalDataset : Dataset = myDataset
	}	
	override def getSupplier : GraphSupplier = mySupplier
	override def getQuerier : GraphQuerier = myQuerier
	override def getAbsorber : GraphAbsorber = myAbsorber
	override def getUpdater : GraphUpdater = myUpdater
}
trait RemoteDelegatingPortal extends RemoteGraphPortal with DelegatingPortal   {
	
	lazy val myAbsorber = new LoggingPortal with RemoteGraphAbsorber {
		override protected def getRemoteDataServiceURL : String = RemoteDelegatingPortal.this.getRemoteDataServiceURL
		override protected def getRemoteUpdateServiceURL : String = RemoteDelegatingPortal.this.getRemoteUpdateServiceURL
		override protected def getRemoteQueryServiceURL : String = { throw new Exception ("Absorber does not support Query")}
	}
	lazy val mySupplierAndQuerier = new LoggingPortal with RemoteGraphSupplierAndQuerier {
		override protected def getRemoteDataServiceURL : String = RemoteDelegatingPortal.this.getRemoteDataServiceURL
		override protected def getRemoteQueryServiceURL : String = RemoteDelegatingPortal.this.getRemoteQueryServiceURL
		override protected def getRemoteUpdateServiceURL : String = { throw new Exception ("Supplier/Querier does not support Update")}
	}
	lazy val myUpdater = new LoggingPortal with RemoteGraphUpdater {
		override protected def getRemoteDataServiceURL : String = RemoteDelegatingPortal.this.getRemoteDataServiceURL
		override protected def getRemoteUpdateServiceURL : String = RemoteDelegatingPortal.this.getRemoteUpdateServiceURL	
		override protected def getRemoteQueryServiceURL : String = { throw new Exception ("Updater does not support Query")}
	}
	override def getAbsorber : GraphAbsorber = myAbsorber
	override def getSupplier : GraphSupplier = mySupplierAndQuerier	
	override def getQuerier : GraphQuerier = mySupplierAndQuerier
	override def getUpdater : GraphUpdater = myUpdater	
}
class LazyRemoteDelegatingPortal(protected val myDefaultRemoteDataSvcURL : String, protected val myDefaultRemoteQuerySvcURL : String,
			protected val myDefaultRemoteUpdateSvcURL : String) extends LoggingPortal with RemoteDelegatingPortal   {
	// override protected def getLocalDataset : Dataset = myDataset
	override protected def getRemoteDataServiceURL : String = myDefaultRemoteDataSvcURL
	override protected def getRemoteQueryServiceURL : String = myDefaultRemoteQuerySvcURL
	override protected def getRemoteUpdateServiceURL : String = myDefaultRemoteUpdateSvcURL
}
