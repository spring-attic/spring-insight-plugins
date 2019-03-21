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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.parsers.OracleRACParser;
import com.springsource.insight.util.ListUtil;


public class OracleRACParserTest extends SqlParserTestImpl<OracleRACParser> {
    public OracleRACParserTest () {
    	super(DatabaseType.ORACLE, new OracleRACParser(),
  			  new SqlTestEntry("jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=yes)"
  					  				+ "(ADDRESS = (PROTOCOL = TCP)(HOST = 108.121.111.114)(PORT = 7365))"
  					  				+ "(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME = RAC.PARSER.TEST)))",
  					  			"108.121.111.114",
  					  			7365,
			  		   			"RAC.PARSER.TEST"));
    }
	
	@Test
	public void testTwoHostsAndPortsAndServiceName() {
		// Basic Oracle RAC JDBC URL
		String connectionUrl = "jdbc:oracle:thin:@(DESCRIPTION = (LOAD_BALANCE = on)(FAILOVER=on) "
							+ "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)(PORT = 1520)) "
							+ "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.139)(PORT = 1521)) "
							+ "(CONNECT_DATA = (SERVER = DEDICATED) (SERVICE_NAME = RAC.WORLD)(FAILOVER_MODE =(TYPE = SELECT)(METHOD = PRECONNECT)(RETRIES = 5)(DELAY = 1)) ) )"
							;
		Map<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put("10.17.184.138", Integer.valueOf(1520));
		hostToPortHash.put("10.17.184.139", Integer.valueOf(1521));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash, "RAC.WORLD");
	}
	
	@Test
	public void testTwoHostsOneInvalidPort(){
		// Basic Oracle RAC JDBC URL
		String connectionUrl =  "jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)(PORT = boat)) "
							+ "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.139)(PORT = 1521)))"
							;
		Map<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put("10.17.184.138", Integer.valueOf(-1));
		hostToPortHash.put("10.17.184.139", Integer.valueOf(1521));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash, null);
	}
	
	@Test
	public void testTwoHostsFirstWithNoPort(){
		// Basic Oracle RAC JDBC URL
		String connectionUrl = "jdbc:oracle:thin:@(DESCRIPTION = "
							 + "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)) "
							 + "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.139)(PORT = 1521)))"
							 ;
		Map<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put("10.17.184.138", Integer.valueOf(parser.getDefaultPort()));
		hostToPortHash.put("10.17.184.139", Integer.valueOf(1521));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash, null);
	}
	
	@Test
	public void testOneAddressWithNoHost(){
		// Basic Oracle RAC JDBC URL
		String connectionUrl = "jdbc:oracle:thin:@(DESCRIPTION = "
								+ "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)(PORT = 1520)) "
								+ "(ADDRESS = (PROTOCOL = TCP)(PORT = 1521)))";
		Map<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put(parser.getDefaultHost(), Integer.valueOf(1521));
		hostToPortHash.put("10.17.184.138", Integer.valueOf(1520));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash, null);
	}

	@Test
	public void testUnspecifiedHostAndPortOnlyServiceName () {
		String connectionUrl = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=yes)"
	  				+ "(ADDRESS = (PROTOCOL = TCP)(HOST = )(PORT = ))"
	  				+ "(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME = ONLY.SERVICE.NAME)))";		
		Map<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put(parser.getDefaultHost(), Integer.valueOf(parser.getDefaultPort()));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash, "ONLY.SERVICE.NAME");
	}

	@Test
	public void testAllDefaultsOnlyServiceName () {
		String connectionUrl = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=yes)"
	  				+ "(ADDRESS = (PROTOCOL = TCP))"
	  				+ "(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME = ONLY.SERVICE.NAME)))";		
		Map<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put(parser.getDefaultHost(), Integer.valueOf(parser.getDefaultPort()));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash, "ONLY.SERVICE.NAME");
	}

	private void testMultipleHostsOrPorts(String connectionUrl, Map<String, Integer> hostToPortHash, String dbName){
		final String				vendorName=databaseType.getVendorName();
		final List<JdbcUrlMetaData> actualJdbcUrlMetaData = parser.parse(connectionUrl, vendorName);
		
		assertEquals("Mismatched number of meta data records", ListUtil.size(actualJdbcUrlMetaData), hostToPortHash.size());
		
		for (JdbcUrlMetaData actual: actualJdbcUrlMetaData) {
			String actualHost = actual.getHost();
			JdbcUrlMetaData	expected=
					new SimpleJdbcUrlMetaData(actualHost, hostToPortHash.get(actualHost).intValue(), dbName, connectionUrl, vendorName);
			assertEquals("Mismatched result for " + connectionUrl, expected, actual); 
		}
	}

}
