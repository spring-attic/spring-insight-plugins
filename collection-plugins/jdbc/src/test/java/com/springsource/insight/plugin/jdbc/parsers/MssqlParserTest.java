/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jdbc.parsers;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.MssqlParser;


public class MssqlParserTest extends SqlParserTestImpl<MssqlParser> {
    public MssqlParserTest() {
        super(DatabaseType.MSSQL, new MssqlParser(),
                new SqlTestEntry("jdbc:microsoft:sqlserver://host:1434;DatabaseName=dbname",
                        "host",
                        1434,
                        "dbname"),
                new SqlTestEntry("jdbc:microsoft:sqlserver://host:1434;DatabaseName=",
                        "host",
                        1434,
                        JdbcUrlParser.DEFAULT_DB_NAME),
                //should it be without the colon? (i.e - jdbc:microsoft:sqlserver://host;DatabaseName=dbname)
                new SqlTestEntry("jdbc:microsoft:sqlserver://host:;DatabaseName=dbname",
                        "host",
                        MssqlParser.DEFAULT_CONNECTION_PORT,
                        "dbname"),
                new SqlTestEntry("jdbc:microsoft:sqlserver://:1434;DatabaseName=dbname",
                        JdbcUrlParser.DEFAULT_HOST,
                        1434,
                        "dbname"),
                new SqlTestEntry("jdbc:sqlserver://10.150.31.43:1433;authenticationScheme=nativeAuthentication;" +
                        "xopenStates=false;sendTimeAsDatetime=true;trustServerCertificate=false;" +
                        "sendStringParametersAsUnicode=true;selectMethod=direct;responseBuffering=adaptive;" +
                        "packetSize=8000;multiSubnetFailover=false;loginTimeout=15;lockTimeout=-1;" +
                        "lastUpdateCount=true;encrypt=false;disableStatementPooling=true;" +
                        "databaseName=moviestore;applicationName=Microsoft JDBC Driver for SQL Server;" +
                        "applicationIntent=readwrite;",
                        "10.150.31.43",
                        1433,
                        "moviestore"));
    }
}
