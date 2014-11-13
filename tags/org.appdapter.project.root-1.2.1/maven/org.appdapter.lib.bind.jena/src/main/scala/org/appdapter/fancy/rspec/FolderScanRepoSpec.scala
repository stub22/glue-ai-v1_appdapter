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

package org.appdapter.fancy.rspec

import java.io.File
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.loader.ExtendedFileLoading.Paths
import org.appdapter.core.loader.SpecialRepoLoader
import org.appdapter.fancy.loader.{FancyRepoLoader}

import org.appdapter.fancy.query.{QueryHelper}
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }

/**
 * @author Douglas R. Miles <www.logicmoo.org>
 * @author Stu B. <www.texpedient.com>
 */

class FolderScanRepoSpec(var dirModelURL: String, fileModelCLs: java.util.List[ClassLoader]) 
			extends MultiRepoSpec(null, fileModelCLs) {
				
			
	def createURIFromBase(fileDirMask: String) = "scan:/" + fileDirMask;

	def populateDirModel(dir0: String) {
		var dir = dir0
		val all = new java.util.ArrayList[RepoSpec]
		val fileFilter = new Paths();
		if (new File(dir).isDirectory()) {
			while (dir.endsWith("\\")) {
				dir = dir.substring(0, dir.length() - 1);
			}
			while (dir.endsWith("/")) {
				dir = dir.substring(0, dir.length() - 1);
			}
			fileFilter.glob(dir + "/", "dir.ttl");
			fileFilter.glob(dir + "/**", "dir.ttl");
		} else {
			fileFilter.glob(".", dir);
		}
		var paths = fileFilter.getFiles();
		for (f <- paths.toArray(new Array[java.io.File](0))) {
			all.add(new URLDirModelRepoSpec(f.getPath(), fileModelCLs));
		}
	}

	override protected def makeDirectoryModel() = {
		populateDirModel(dirModelURL)
		super.makeDirectoryModel
	}

	override def toString = if (toStringName != null) toStringName else createURIFromBase(dirModelURL)
}

