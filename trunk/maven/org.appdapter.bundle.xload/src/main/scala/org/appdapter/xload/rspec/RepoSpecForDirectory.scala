/*
 *  Copyright 2015 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.xload.rspec

import org.appdapter.fancy.rspec.RepoSpec
import org.appdapter.xload.fancy.{FancyRepoLoader, InstallableRepoLoader}
import org.appdapter.fancy.repo.{ FancyRepo }

/**
 *  In the case of RepoSpecForDirectory, assume that a dir-model will be found first,
 *  and we will build the repo around it.
 */
abstract class RepoSpecForDirectory extends RepoSpec {
  override protected def makeRepo: FancyRepo = {
    FancyRepoLoader.makeRepoWithDirectory(this, getOrMakeDirectoryModel(), null);
  }
   override def getOrMakeRepo: FancyRepo =  makeRepo
  
}
/**
 grep -rin    --include=*.{java,scala,xml,ttl,owl,html} --exclude="*\.svn*" --exclude-dir=target RepoSpecForDir appda
pter_trunk/maven/ cogchar_trunk/maven/ friendularity_trunk/maven/ 

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/CSVFileRepoSpec.scala:35:  fi
leModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/GoogSheetRepoSpec.scala:21://
 import org.appdapter.fancy.rspec.{RepoSpec, RepoSpecForDirectory}
 
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/GoogSheetRepoSpec.scala:37:
fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/GoogSheetRepoSpec.scala:43:
fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/MultiRepoSpec.scala:27:  exte
nds RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/OfflineXlsSheetRepoSpec.scala
:21:  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/RepoSpec.scala:72:abstract cl
ass RepoSpecForDirectory extends RepoSpec {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/UrlDirModelRepoSpec.scala:73:
class URLDirModelRepoSpec(dirModelURL: String, fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {


 */
