
package org.appdapter.core.matdat

import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.log.BasicDebugger
import org.appdapter.help.repo.RepoLoader

@deprecated
object RepoTester extends RepoLoader {
}

@deprecated
class RepoTester extends RepoLoader {
}

@deprecated
object CsvFilesSheetRepo extends CsvFilesSheetRepoLoader {

}

@deprecated
class CsvFilesSheetRepo extends CsvFilesSheetRepoLoader {

}

@deprecated
class XLSXSheetRepo(dirModel: Model,
  fileModelCLs: java.util.List[ClassLoader]) extends SheetRepo(dirModel, fileModelCLs) {

  def this(sheetLocation: String, namespaceSheet: String, dirSheet: String, fileModelCLs: java.util.List[ClassLoader]) {
    this(XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheet, dirSheet, fileModelCLs), fileModelCLs)
  }

}

@deprecated
object XLSXSheetRepo {
  def readDirectoryModelFromXLSX(sheetLocation: String, namespaceSheet: String, dirSheet: String, fileModelCLs: java.util.List[ClassLoader]): Model = {
    XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheet, dirSheet, fileModelCLs)
  }
}

@deprecated
object GoogSheetRepo extends BasicDebugger {
  def readDirectoryModelFromGoog(sheetLocation: String, namespaceSheet: Int, dirSheet: Int): Model = {
    // Read the single directory sheets into a single directory model.
    GoogSheetRepoLoader.readModelFromGoog(sheetLocation, namespaceSheet, dirSheet)
  }
}

/*
@deprecated
class XLSXSheetRepo(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader])
  extends XLSXSheetRepo(XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheet, dirSheet, fileModelCLs), fileModelCLs) {

}*/
/*
class XLSXSheetRepoSpec(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader] = null)
  extends OfflineXlsSheetRepoSpec(sheetLocation, namespaceSheet, dirSheet, fileModelCLs) {

}*/
/*
package org.cogchar.gui.demo {
  class DemoNavigatorCtrl(bc: BoxContext, tm: TreeModel, rootBTN: ScreenBoxTreeNode, dcp: DisplayContextProvider)
    extends org.appdapter.gui.demo.DemoNavigatorCtrl(bc, tm, rootBTN, dcp) {
  }
 }
*/

