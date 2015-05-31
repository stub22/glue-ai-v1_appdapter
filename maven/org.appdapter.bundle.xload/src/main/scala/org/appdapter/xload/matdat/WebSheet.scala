/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.xload.matdat

/**
 * @author Stu B. <www.texpedient.com>
 */
/////////////////////////////////////////
/// Goog Doc Sheet Reading
/////////////////////////////////////////

object WebSheet {
  final private[matdat] val gdocSheetBaseURL = "https://docs.google.com/spreadsheet/pub";
  //  Building a param string like ?key=0ArBjkBo&single=true&gid=7&range=A2%3AK999&output=csv
  final private[matdat] val gdocParamDocKey = "key";
  final private[matdat] val gdocParamSingleSheet = "single";
  final private[matdat] val gdocParamSheetNum = "gid";
  final private[matdat] val gdocParamCellRange = "range";
  final private[matdat] val gdocParamOutputFormat = "output";
  final private[matdat] val gdocFormatCSV = "csv";
  final private[matdat] val gdocFlagTrue = "true";
  final private[matdat] val queryIndicator = "?";
  final private[matdat] val paramSeperator = "&";
  final private[matdat] val paramAssign = "=";

  final private[matdat] def makeParamBinding(name: String, v: String) = name + paramAssign + v;

  final def makeGdocSheetQueryURL(docKey: String, sheetNum: Int, optRange: Option[String]): String = {
    val builder = new StringBuilder(gdocSheetBaseURL)
    builder.append(queryIndicator);
    builder.append(makeParamBinding(gdocParamDocKey, docKey));
    builder.append(paramSeperator);
    builder.append(makeParamBinding(gdocParamSheetNum, sheetNum.toString));
    if (optRange.isDefined) {
      builder.append(paramSeperator);
      builder.append(makeParamBinding(gdocParamCellRange, optRange.get));
    }
    builder.append(paramSeperator);
    builder.append(makeParamBinding(gdocParamOutputFormat, gdocFormatCSV));
    builder.append(paramSeperator);
    builder.append(makeParamBinding(gdocParamSingleSheet, gdocFlagTrue));

    builder.toString();
  }

}