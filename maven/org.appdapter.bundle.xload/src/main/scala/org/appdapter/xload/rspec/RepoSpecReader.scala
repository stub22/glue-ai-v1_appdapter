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

package org.appdapter.xload.rspec

import org.appdapter.core.log.BasicDebugger

import org.appdapter.fancy.rspec.RepoSpec

abstract class RepoSpecReader extends BasicDebugger {
  def getExt(): String;
  def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]): RepoSpec;
}

/* Confusing use of "dir" as an "Ext" - what is going on here?
*/
class RSpecReader_UrlDir_Dir extends RepoSpecReader {
  override def getExt = "dir"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
}

class RSpecReader_UrlDir_Turtle extends RepoSpecReader {
  override def getExt = "ttl"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
}

class RSpecReader_FolderScan_Dir extends RepoSpecReader {
  override def getExt = "scandir"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new FolderScanRepoSpec(path, cLs)
}
class RSpecReader_FolderScan_Turtle extends RepoSpecReader {
  override def getExt = "scanttl"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new FolderScanRepoSpec(path, cLs)
}