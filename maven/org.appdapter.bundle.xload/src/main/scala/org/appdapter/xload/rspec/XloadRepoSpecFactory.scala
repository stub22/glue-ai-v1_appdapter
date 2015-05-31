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

import org.appdapter.fancy.rspec.RepoSpecDefaultNames._
import org.osgi.framework.BundleContext
import org.appdapter.core.boot.ClassLoaderUtils

object XloadRepoSpecFactory {
  def makeBMC_RepoSpec(ctx: BundleContext): OnlineSheetRepoSpec = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    makeBMC_RepoSpec(fileResModelCLs);
  }
  def makeBMC_RepoSpec(fileResModelCLs: java.util.List[ClassLoader]): OnlineSheetRepoSpec = {
    new OnlineSheetRepoSpec(BMC_SHEET_KEY, BMC_NAMESPACE_SHEET_NUM, BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
  }

  def makeBMC_OfflineRepoSpec(ctx: BundleContext): OfflineXlsSheetRepoSpec = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    makeBMC_OfflineRepoSpec(fileResModelCLs);
  }

  def makeBMC_OfflineRepoSpec(fileResModelCLs: java.util.List[ClassLoader]): OfflineXlsSheetRepoSpec = {
    new OfflineXlsSheetRepoSpec(BMC_WORKBOOK_PATH, DFLT_NAMESPACE_SHEET_NAME, DFLT_DIRECTORY_SHEET_NAME, fileResModelCLs);
  }
}
