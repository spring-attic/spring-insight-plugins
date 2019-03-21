/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
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
import com.springsource.insight.plugin.jdbc.parser.parsers.OracleParser;


public class OracleParserTest extends SqlParserTestImpl<OracleParser> {
	public OracleParserTest () {
		super(DatabaseType.ORACLE, new OracleParser(),
			  // 1st format
			  new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@//10.1.1.1:1522/XE",
							   "10.1.1.1",
							   1522,
					  		   "XE"),
			  new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@//:1521/XE",
			  		   		   JdbcUrlParser.DEFAULT_HOST,
			  		   		   OracleParser.DEFAULT_CONNECTION_PORT,
					           "XE"),
			  new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@///XE",
					  		   JdbcUrlParser.DEFAULT_HOST,
					  		   OracleParser.DEFAULT_CONNECTION_PORT,
					  		   "XE"),
			  new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@//localhost/XE",
					  		   "localhost",
					  		   OracleParser.DEFAULT_CONNECTION_PORT,
					  		   "XE"),

			  // 2nd format
			  new SqlTestEntry("jdbc:oracle:thin:@172.16.1.9:1522:orcl",
					  		   "172.16.1.9",
					  		   1522,
					  		   "orcl"),
		      new SqlTestEntry("jdbc:oracle:thin:@:1522:orcl",
			  		   		   JdbcUrlParser.DEFAULT_HOST,
		    		 		   1522,
		    		 		   "orcl"),
		      new SqlTestEntry("jdbc:oracle:thin:@:orcl",
	  		   		   		   JdbcUrlParser.DEFAULT_HOST,
	  		   		   		   OracleParser.DEFAULT_CONNECTION_PORT,
		    		  		   "orcl"),
		      new SqlTestEntry("jdbc:oracle:thin:@localhost:orcl",
		    		  		   "localhost",
	  		   		   		   OracleParser.DEFAULT_CONNECTION_PORT,
		    		  		   "orcl"),
		
		      // 2nd format with user and password
		      new SqlTestEntry("jdbc:oracle:thin:username/pass@oradev.metadyne.uk.com:1021:oradev",
		    		  		   "oradev.metadyne.uk.com",
		    		  		   1021,
		    		  		   "oradev"));
	}
}
