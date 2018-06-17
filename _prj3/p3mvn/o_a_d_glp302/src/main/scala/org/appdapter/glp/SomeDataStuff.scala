package org.appdapter.glp

import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.RDFDataMgr

class SomeDataStuff {
	val thatModelPath = "gdat/glp_dat_01_owl.ttl"
	def loadThatModel() : Model = {
		val mdl = RDFDataMgr.loadModel(thatModelPath)
		mdl
	}

}
