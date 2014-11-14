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


import org.appdapter.bind.rdf.jena.query.{JenaArqQueryFuncs_TxAware}
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.appdapter.fancy.log.VarargsLogging

/**
 * @author Stu B. <www.texpedient.com>
 */

case class CopyOpResult() {
	
}
object GraphPortalFuncs {
	def copyGraphs_Naive(sourceGS : GraphSupplier, optSrcGraphChooser : Option[SGS_Filter], tgtUriMapper : SGS_Mapper, 
						targetGA : GraphAbsorber, replaceInTgt : Boolean) : CopyOpResult = {
	   val result = new CopyOpResult
		val vl = targetGA.getVarargsLogger
		val srcStats = sourceGS.fetchStats(optSrcGraphChooser)
		srcStats foreach (gs => {
			val optTgtGraphURI = tgtUriMapper(gs)
			if (optTgtGraphURI.isDefined) {
				val tgtGraphURI : String = optTgtGraphURI.get
				vl.info4("Doing import from: {} to: {} in: {}, replaceInTgt={} ", gs, tgtGraphURI, targetGA, replaceInTgt : java.lang.Boolean);
				// val tgtModelID = txRepo.makeIdentForURI(gs.graphUR)
				val srcGraphURI = gs.myAbsUriTxt
				
				// val tgtModelID =  inputRepo.makeIdentForURI(srcGraphURI)
				val srcModel = sourceGS.getNamedGraph_Readonly(srcGraphURI)// inputRepo.getNamedModel(tgtModelID)
				vl.debug1("Fetched source model of size {} statements", srcModel.size : java.lang.Long)
				if (replaceInTgt) {
					targetGA.replaceNamedModel(tgtGraphURI, srcModel)
				} else {
					targetGA.addStatementsToNamedModel(tgtGraphURI, srcModel)
				}
			} else {
				vl.debug1("Mapper said DONT COPY: {}", gs)
			}
		})
		result;
	}
	def bracketedPortalTransfer[RT](readOnlySupplier : GraphSupplier, writableAbsorber : GraphAbsorber, op : Oper[RT],
				onFailure : RT) : RT = {
		val writeOp = new Oper[RT]() { 
			override def perform(): RT = {
				readOnlySupplier.execReadTransCompatible(op, onFailure)
			}
		}
		writableAbsorber.execWriteTransCompatible(writeOp, onFailure)
	}
	def copyGraphs_SingleTx(sourceGS : GraphSupplier, optSrcGraphChooser : Option[SGS_Filter], tgtUriMapper : SGS_Mapper, 
						targetGA : GraphAbsorber, replaceInTgt : Boolean) : CopyOpResult = {
		val copyOp = new Oper[CopyOpResult]() { 
			override def perform(): CopyOpResult = {
				copyGraphs_Naive(sourceGS, optSrcGraphChooser, tgtUriMapper, targetGA, replaceInTgt)
			}
		}
		bracketedPortalTransfer(sourceGS, targetGA, copyOp, null)		
	}
	
	def copyGraphsAndShowStats(sourcePortal : DelegatingPortal, targetPortal : DelegatingPortal, flag_overwriteTgtGraphs : Boolean) {
		val vl = targetPortal.getVarargsLogger
		val sourceGS = sourcePortal.getSupplier
		val sourceStats = sourceGS.fetchStats(None)
		vl.info2("Got {} sourceStats: {}", sourceStats.length : java.lang.Integer, sourceStats)
		val targetGS = targetPortal.getSupplier
		val tgtStatsBefore = targetGS.fetchStats(None)
		val targetGA = targetPortal.getAbsorber
		
		val tgtUriChooseFunc = new SGS_Mapper() { 
			override def apply(sgs:SuppliedGraphStat) = Some(sgs.myAbsUriTxt)
		}
	 
		copyGraphs_SingleTx(sourceGS, None, tgtUriChooseFunc, targetGA,  flag_overwriteTgtGraphs)
		// AbsorberFuncs.copyRepoGraphsToLocalDset_OneTx(legacyRepo, tgtUriChooseFunc, inMemDSet, false)
		
		val tgtStatsAfter  = targetGS.fetchStats(None)
		
		val postStatSet = tgtStatsAfter.toSet
		val sourceStatSet = sourceStats.toSet
		val diff1 = sourceStatSet.diff(postStatSet)
		vl.info1("sourceStats.diff(postStats) = {} ", diff1)
		val diff2 = postStatSet.diff(sourceStatSet)
		vl.info1("postStats.diff(sourceStats) = {} ", diff2)		
	}
	
}
/*		// TODO:  add respect for existing xact.
		if (!tgtDataset.supportsTransactions) {
			throw new RuntimeException("This method expects a transaction-aware target dataset")
		}
		tgtDataset.begin(ReadWrite.WRITE);
		val locAbsorber = new LocalGraphAbsorber(tgtDataset);
		
		val inputMQDset = inputRepo.getMainQueryDataset
		inputMQDset.begin(ReadWrite.READ);
		try {
			copyRepoGraphsToAbsorber(inputRepo, tgtUriChooser, locAbsorber, replaceInTgt)
			tgtDataset.commit
		} catch {
			case t : Throwable => {
				getLogger.error("Caught error, rolling back", t)
				tgtDataset.abort()
			}
		} finally {
			// If we haven't committed or aborted, this will show:
			// 21571 [main] WARN TDB  - Transaction not commited or aborted: Transaction: 2 : Mode=WRITE : State=ACTIVE : --mem--/
			// ...and implicity abort.
			tgtDataset.end()
			inputMQDset.end();
		}
	}
*/	
