/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.trigger;

import com.appdapter.binding.h2.DatabaseConnector;
import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.TriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author winston
 */
public class DatabaseTriggers {
	static Logger theLogger = LoggerFactory.getLogger(DatabaseTriggers.class);
	public enum Kind {
		OPEN,
		CLOSE
	}

	public static class InitTrigger<BT extends Box<TriggerImpl<BT>>> extends  TriggerImpl<BT> {
		@Override public void fire(Box targetBox) {
			theLogger.info(toString() + "-initing");

			DatabaseConnector.Config dbcc = new DatabaseConnector.Config();
			dbcc.dbFilePath = "testdata/h2/td01";
			dbcc.dbUser = "sa";
			dbcc.dbPassword = "";
			dbcc.tcpPort = "9330";
			dbcc.webPort = "9331";
			DatabaseConnector dbc = new DatabaseConnector();

			try {
				dbc.init(dbcc);
			} catch (Throwable t) {
				theLogger.error("Problem in DatabaseConnector init", t);
			}
			theLogger.info("DatabaseConnector.init() appears to have succeeded, try a web connection.");
		}
	}
}
