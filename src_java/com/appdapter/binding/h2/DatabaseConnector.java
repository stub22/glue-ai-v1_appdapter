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

package com.appdapter.binding.h2;
import java.sql.Connection;
import java.sql.DriverManager;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class DatabaseConnector {

	public static class Config {
		public		String	dbFilePath;
		public		String	dbUser;
		public		String  dbPassword;
		public		String	tcpPort;
		public		String	webPort;
	}

	static Logger theLogger = LoggerFactory.getLogger(DatabaseConnector.class);
	private		Connection		myConn;
	private		Server			myTcpServer;
	private		Server			myWebServer;

	public void init(Config conf) throws Throwable {
		myTcpServer = makeTcpServer(conf.tcpPort);
		myWebServer = makeWebServer(conf.webPort);

		myConn = makeConnection(conf.dbFilePath, conf.dbUser, conf.dbPassword);
		myTcpServer.start();
		myWebServer.start();
	}
	protected void stop() throws Throwable {
		myTcpServer.stop();
		myConn.close();
	}
	protected Connection makeConnection(String dbFilePath, String dbUser, String dbPassword) throws Throwable {
		// Load the driver
		Class.forName("org.h2.Driver").newInstance();
		Connection conn = DriverManager.getConnection("jdbc:h2:file:" + dbFilePath, dbUser, dbPassword);
		return conn;
	}
	protected Server makeTcpServer(String tcpPort) throws Throwable {
		Server tcpServer = Server.createTcpServer("-tcpAllowOthers", "true", "-tcpPort", tcpPort);
		return tcpServer;
	}
	protected Server makeWebServer(String webPort) throws Throwable {
		Server tcpServer = Server.createTcpServer("-webAllowOthers", "false", "-webPort", webPort);
		return tcpServer;
	}
}

/*
 * $ java -cp dist/lib/h2-1.2.142.jar org.h2.tools.Server -?
Starts the H2 Console (web-) server, TCP, and PG server.
Usage: java org.h2.tools.Server <options>
When running without options, -tcp, -web, -browser and -pg are started.
Options are case sensitive. Supported options are:
[-help] or [-?]         Print the list of options
[-web]                  Start the web server with the H2 Console
[-webAllowOthers]       Allow other computers to connect - see below
[-webDaemon]            Use a daemon thread
[-webPort <port>]       The port (default: 8082)
[-webSSL]               Use encrypted (HTTPS) connections
[-browser]              Start a browser connecting to the web server
[-tcp]                  Start the TCP server
[-tcpAllowOthers]       Allow other computers to connect - see below
[-tcpDaemon]            Use a daemon thread
[-tcpPort <port>]       The port (default: 9092)
[-tcpSSL]               Use encrypted (SSL) connections
[-tcpPassword <pwd>]    The password for shutting down a TCP server
[-tcpShutdown "<url>"]  Stop the TCP server; example: tcp://localhost
[-tcpShutdownForce]     Do not wait until all connections are closed
[-pg]                   Start the PG server
[-pgAllowOthers]        Allow other computers to connect - see below
[-pgDaemon]             Use a daemon thread
[-pgPort <port>]        The port (default: 5435)
[-baseDir <dir>]        The base directory for H2 databases (all servers)
[-ifExists]             Only existing databases may be opened (all servers)
[-trace]                Print additional trace information (all servers)
The options -xAllowOthers are potentially risky.
For details, see Advanced Topics / Protection against Remote Access.
See also http://h2database.com/javadoc/org/h2/tools/Server.html
 */