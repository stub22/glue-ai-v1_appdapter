/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.rdf.jena.sdb;

import java.sql.Connection;
import java.sql.DriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author Stu B. <www.texpedient.com>
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
