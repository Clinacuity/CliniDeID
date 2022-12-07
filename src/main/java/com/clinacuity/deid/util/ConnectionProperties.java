
/*
# © Copyright 2019-2022, Clinacuity Inc. All Rights Reserved.
#
# This file is part of CliniDeID.
# CliniDeID is free software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# either version 3 of the License, or any later version.
# CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with CliniDeID.
# If not, see <https://www.gnu.org/licenses/>.
# =========================================================================   
*/

package com.clinacuity.deid.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.resource.ResourceInitializationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class ConnectionProperties {
    public static final Set<String> SUPPORTED_DB = Set.of("mysql", "postgresql", "db2", "ms sql server", "sqlserver");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DB_OUTPUT_TABLE_NAME = "clinideid";
    private static final String DB_OUTPUT_TYPE = "postgresql";
    private static volatile ConnectionProperties instance = null;//Sonarcube said having this as volatile wasn't good b/c only reference was in memory, not object's contents;
    // leaving it as volatile b/c that was in examples
    private static Connection connection = null;
    private static Object mutex = new Object();


    private ConnectionProperties() {
    }

    public static boolean supportedDbms(String dbms) {
        return SUPPORTED_DB.contains(dbms);
    }

    public static Connection makeConnection(String dbType, String server, String prt, String name, String user, String pass, String schem) throws ResourceInitializationException {//schema? server? port? name?
        String dbms = dbType.toLowerCase();
        Connection connection;
        String url;
        try {
            if ("mysql".equals(dbms)) {
                //Class.forName("com.mysql.jdbc.Driver");  // jdbc:mysql://<server>:<port>/<databaseName>
                url = makeUrl("jdbc:mysql:", "//", server, prt, "/", name) + "?useLegacyDatetimeCode=false&serverTimezone=UTC";
            } else if ("postgresql".equals(dbms)) {
                //Class.forName("org.postgresql.Driver");//jdbc:postgresql://<server>:<port>/<databaseName>
                url = makeUrl("jdbc:postgresql:", "//", server, prt, "/", name);
            } else if (dbms.contains("db2")) {
                Class.forName("com.ibm.db2.jcc.DB2Driver"); //jdbc:db2//<server>:<port>/<databasebName>
                url = makeUrl("jdbc:db2:", "//", server, prt, "/", name);
            } else if ("ms sql server".equals(dbms) || "sqlserver".equals(dbms)) {
                //  Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");   //dbc:sqlserver://<server>:<port>;databaseName=<databaseName>
                url = makeUrl("jdbc:sqlserver:", "//", server, prt, ";databaseName=", name);
//            } else if ("oracle".equals(dbms)) {
//                Class.forName("oracle.jdbc.driver.OracleDriver");//jdbc:oracle:thin:@<server>:<port>:<databaseName>
//                url = makeUrl("jdbc:oracle:thin:", "@", server, "1522", "/", name);
//            } else if ("sybase".equals(dbms)) {
//                Class.forName("net.sourceforge.jtds.jdbc.Driver");//com.sybase.jdbc.SybDriver");  //jdbc:sybase:Tds:<server>:<port>/<databaseName>
//                url = makeUrl("jdbc:sybase:Tds", ":", server, prt, "/", name);
//            } else if ("teradata".equals(dbms)) {
//                Class.forName("com.teradata.jdbc.TeraDriver");  //jdbc:teradata://<server>/database=<databaseName>,tmode=ANSI,charset=UTF8
//                url = "jdbc:teradata://" + server + "/database=" + name + ",tmode=ANSI,charset=UTF8";
//            } else if (dbms.contains("access")) {
//                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");  // jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=<myDBName.mdb>;
//                url = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=<" + name + ".mdb>;";
            } else {
                LOGGER.error("{} not a recognized DBMS", dbms);
                throw new ResourceInitializationException("DBMS not recognized:  " + dbms, null);
            }
            if (!user.isEmpty()) {
                connection = DriverManager.getConnection(url, user, pass);
            } else {
                connection = DriverManager.getConnection(url);
            }
            if (!schem.isEmpty()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeQuery(schem);//"ALTER SESSION SET CURRENT_SCHEMA = " + schem);
                }
            }
        } catch (ClassNotFoundException | NullPointerException | SQLException e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }
        return connection;
    }

    private static String makeUrl(String base, String serverPrefix, String server, String prt, String namePrefix, String name) {
        if (server.isEmpty()) {
            return base + name;
        } else {
            return base + serverPrefix + server + ":" + prt + namePrefix + name;
        }
    }

    public static ConnectionProperties getInstance() throws ResourceInitializationException {
        try {
            ConnectionProperties result = instance;
            if (result == null || connection == null || connection.isClosed()) {
                synchronized (mutex) {
                    result = instance;
                    if (result == null) {
                        connection = makeConnection(DB_OUTPUT_TYPE, "", "", DB_OUTPUT_TABLE_NAME, "", "", "");
                        instance = new ConnectionProperties();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.throwing(e);
            throw new ResourceInitializationException(e);
        }
        return instance;
    }

    public static String dbOutputCheck() {
        try {
            Connection con = ConnectionProperties.getInstance().getConnection();//Can't be in try w/ resources as don't want to close connection
            try (Statement stmt = con.createStatement();//This is safe as query is constant
                 ResultSet rs = stmt.executeQuery("select exists(SELECT datname FROM pg_catalog.pg_database WHERE datname = '" + DB_OUTPUT_TABLE_NAME + "' )")) {
                if (!rs.next() || !"t".equalsIgnoreCase(rs.getString(1))) {
                    LOGGER.error("DB output failed to find output table");
                    return "database " + DB_OUTPUT_TABLE_NAME + " not found";
                }
            }
        } catch (ResourceInitializationException | SQLException e) {
            LOGGER.throwing(e);
            return "couldn't connect to database for output (see README file for instructions to create it)";
        }
        return "";
    }

    public Connection getConnection() {
        return connection;
    }
}
