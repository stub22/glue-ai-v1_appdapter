/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.appdapter.core.matdat
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo, InitialBinding}
import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBindingImpl} 
import org.appdapter.impl.store.{FancyRepo, DatabaseRepo, FancyRepoFactory};
import org.appdapter.core.matdat.{SheetRepo, GoogSheetRepo, XLSXSheetRepo, OmniLoaderRepo}
import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}
import org.appdapter.core.log.BasicDebugger;
/**
 * @author Stu B. <www.texpedient.com>
 */

object RepoLoader extends BasicDebugger {
	// Modeled on SheetRepo.loadTestSheetRepo
	def loadGoogSheetRepo(sheetKey : String, namespaceSheetNum : Int, dirSheetNum : Int, 
						fileModelCLs : java.util.List[ClassLoader]) : SheetRepo = {
		// Read the namespaces and directory sheets into a single directory model.
		val dirModel : Model = GoogSheetRepo.readDirectoryModelFromGoog(sheetKey, namespaceSheetNum, dirSheetNum) 
		// Construct a repo around that directory        
        //val shRepo = new GoogSheetRepo(dirModel);
        // Doug's locally testing this replacement
		val spec = new OnlineSheetRepoSpec(sheetKey,namespaceSheetNum,dirSheetNum,fileModelCLs);
        val shRepo = new OmniLoaderRepo(spec, "goog:" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum, dirModel, fileModelCLs)
		// Load the rest of the repo's initial *sheet* models, as instructed by the directory.
		getLogger().debug("Loading Sheet Models") 
		shRepo.loadSheetModelsIntoMainDataset()
		// Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
		//getLogger().debug("Loading File Models")
		//shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
		shRepo
	}
	
		// Modeled on SheetRepo.loadTestSheetRepo
	def loadXLSXSheetRepo(sheetLocation : String, namespaceSheetName : String, dirSheetName : String, 
						fileModelCLs : java.util.List[ClassLoader]) : SheetRepo = {
		// Read the namespaces and directory sheets into a single directory model.
		val dirModel : Model = XLSXSheetRepo.readDirectoryModelFromXLSX(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs) 
		// Construct a repo around that directory
        //val shRepo = new XLSXSheetRepo(dirModel, fileModelCLs);   
		// Doug's locally testing this replacement   
		val spec = new OfflineXlsSheetRepoSpec(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs);
        val shRepo = new OmniLoaderRepo(spec, "xlsx:" + sheetLocation + "/" + namespaceSheetName + "/" + dirSheetName, dirModel, fileModelCLs)
		// Load the rest of the repo's initial *sheet* models, as instructed by the directory.
		getLogger().debug("Loading Sheet Models") 
		shRepo.loadSheetModelsIntoMainDataset()
		// Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
		//getLogger().debug("Loading File Models")
		//shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
		shRepo
	}
	
	def loadDatabaseRepo(configPath : String, optConfigResolveCL : ClassLoader, dirGraphID : Ident) : DatabaseRepo = {
		 val dbRepo = FancyRepoFactory.makeDatabaseRepo(configPath, optConfigResolveCL, dirGraphID)
		 dbRepo;
	}
	
	def testRepoDirect(repo : Repo.WithDirectory, querySheetQName : String, queryQName: String, tgtGraphSparqlVN: String, tgtGraphQName : String) : Unit = {
		// Here we manually set up a binding, as you would usually allow RepoClient
		// to do for you, instead:
		val qib : InitialBinding = repo.makeInitialBinding
		qib.bindQName(tgtGraphSparqlVN, tgtGraphQName)
		
		// Run the resulting fully bound query, and print the results.		
		val solnJavaList : java.util.List[QuerySolution] = repo.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

		println("Found solutions for " + queryQName + " in " + tgtGraphQName + " : " + solnJavaList)
	}
	
	def copyAllRepoModels(sourceRepo : Repo.WithDirectory, targetRepo : Repo.WithDirectory) : Unit = {
	}
}