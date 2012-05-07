/**
 * Copyright 2009-2011 the original author or authors.
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

import org.junit.Before;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.parsers.OracleParser;


public class OracleParserTest extends SqlParserTestImpl {

	
	@Before
	public void setup() {

		parser = new OracleParser();

		// 1st format
		testCases.add(new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@//10.1.1.1:1522/XE",
									  "10.1.1.1",
									  1522,
									  "XE"));
		testCases.add(new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@//:1521/XE",
									  "localhost",
									  1521,
									  "XE"));
		testCases.add(new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@///XE",
									  "localhost",
									  1521,
									  "XE"));

		testCases.add(new SqlTestEntry("jdbc:oracle:thin:Herong/TopSecret@//localhost/XE",
									  "localhost",
									  1521,
									  "XE"));

		// 2nd format
		testCases.add(new SqlTestEntry("jdbc:oracle:thin:@172.16.1.9:1522:orcl",
														 "172.16.1.9",
														 1522,
														 "orcl"));
		testCases.add(new SqlTestEntry("jdbc:oracle:thin:@:1522:orcl",
															"localhost",
															1522,
															"orcl"));
		testCases.add(new SqlTestEntry("jdbc:oracle:thin:@:orcl",
																		 "localhost",
																		 1521,
																		 "orcl"));

		testCases.add(new SqlTestEntry("jdbc:oracle:thin:@localhost:orcl",
															"localhost",
															1521,
															"orcl"));
		
		// 2nd format with user and password
		testCases.add(new SqlTestEntry("jdbc:oracle:thin:username/pass@oradev.metadyne.uk.com:1021:oradev",
														 "oradev.metadyne.uk.com",
														 1021,
														 "oradev"));
	}
	
	@Override
	public DatabaseType getType() {
		return DatabaseType.ORACLE;
	}

}
