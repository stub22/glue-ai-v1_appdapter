/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.gui.repo;

import com.appdapter.gui.repo.RepoBox.GraphStat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RepoGraphTableModel extends AbstractTableModel {
	static Logger theLogger = LoggerFactory.getLogger(RepoGraphTableModel.class);
	static String theColNames[] = {"name", "size", "age-update", "strength/status", "content summary"};
	static Class theColClasses[] = {String.class, Long.class, Double.class, String.class, String.class};

	private	List<GraphStat>	myCachedStats = new ArrayList<GraphStat>();

	protected void refreshStats(RepoBox repoBox) {
		myCachedStats = repoBox.getGraphStats();
		fireTableDataChanged();
	}
	protected void focusOnRepo(RepoBox repoBox) {
		theLogger.info("Focusing on repo-box: " + repoBox);
		refreshStats(repoBox);
	}
	@Override public String getColumnName(int cidx) {
		return theColNames[cidx];
	}
	@Override public Class getColumnClass(int colIndex) {
		return theColClasses[colIndex];
	}
	@Override public int getRowCount() {
		return myCachedStats.size();
	}
	@Override public int getColumnCount() {
		return 5;
	}
	@Override public Object getValueAt(int rowIndex, int columnIndex) {
		GraphStat stat = myCachedStats.get(rowIndex);
		switch(columnIndex) {
			case 0: return stat.graphURI;
			case 1: return new Long(stat.statementCount);
			default: return new Double(-1.0);
		}
	}

}
