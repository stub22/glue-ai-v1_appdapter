/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.binding.jena.sdb;

import java.sql.Connection;
import java.sql.DriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author winston
 */
public class MetaRepo {
	static Logger theLogger = LoggerFactory.getLogger(MetaRepo.class);

	private		String		myH2_JDBC_URL;
	private		String		mySDB_ConfigPath;
	private		Connection	myDBC;


}
/*
 * To use command line scripts, see the scripts page including setting environment variables SDBROOT, SDB_USER, SDB_PASSWORD and SDB_JDBC.
 bin/sdbconfig --sdb=sdb.ttl --create
and run the test suite:
 bin/sdbtest --sdb=sdb.ttl testing/manifest-sdb.ttl
 *
 *
 */
