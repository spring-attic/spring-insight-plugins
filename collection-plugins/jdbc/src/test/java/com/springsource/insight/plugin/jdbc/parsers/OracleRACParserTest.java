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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.parsers.OracleRACParser;


public class OracleRACParserTest extends SqlParserTestImpl<OracleRACParser> {
    public OracleRACParserTest () {
    	super(DatabaseType.ORACLE, new OracleRACParser());
    }
	
	@Test
	public void testTwoHostsAndPorts() {
		// Basic Oracle RAC JDBC URL
		String connectionUrl =  "jdbc:oracle:thin:@(DESCRIPTION = (LOAD_BALANCE = on)(FAILOVER=on) " +
        "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)(PORT = 1520)) " +
        "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.139)(PORT = 1521)) " +
        "(CONNECT_DATA = (SERVER = DEDICATED) (SERVICE_NAME = RAC.WORLD)(FAILOVER_MODE =(TYPE = SELECT)(METHOD = PRECONNECT)(RETRIES = 5)(DELAY = 1)) ) )";
		HashMap<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put("10.17.184.138", Integer.valueOf(1520));
		hostToPortHash.put("10.17.184.139", Integer.valueOf(1521));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash);
	}
	
	@Test
	public void testTwoHostsOneInvalidPort(){
		// Basic Oracle RAC JDBC URL
		String connectionUrl =  "jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)(PORT = boat)) " +
        "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.139)(PORT = 1521)))";
		HashMap<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put("10.17.184.138", Integer.valueOf(-1));
		hostToPortHash.put("10.17.184.139", Integer.valueOf(1521));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash);
	}
	
	@Test
	public void testTwoHostsFirstWithNoPort(){
		// Basic Oracle RAC JDBC URL
		String connectionUrl =  "jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)) " +
                    "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.139)(PORT = 1521)))";
		HashMap<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put("10.17.184.138", Integer.valueOf(-1));
		hostToPortHash.put("10.17.184.139", Integer.valueOf(1521));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash);
	}
	
	@Test
	public void testOneAddressWithNoHost(){
		// Basic Oracle RAC JDBC URL
		String connectionUrl =   "jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)(PORT = 1520)) " +
        "(ADDRESS = (PROTOCOL = TCP)(PORT = 1521)))";
		HashMap<String, Integer> hostToPortHash = new HashMap<String, Integer>(); 
		hostToPortHash.put("10.17.184.138", Integer.valueOf(1520));
		testMultipleHostsOrPorts(connectionUrl, hostToPortHash);
	}
	
	private void testMultipleHostsOrPorts(String connectionUrl, Map<String, Integer> hostToPortHash){
		final String				vendorName=databaseType.getVendorName();
		final List<JdbcUrlMetaData> actualJdbcUrlMetaData = parser.parse(connectionUrl, vendorName);
		
		assertEquals(actualJdbcUrlMetaData.size(), hostToPortHash.size());
		
		for (JdbcUrlMetaData actual: actualJdbcUrlMetaData) {
			String actualHost = actual.getHost();
			JdbcUrlMetaData	expected=
					new SimpleJdbcUrlMetaData(actualHost, hostToPortHash.get(actualHost).intValue(), null, connectionUrl, vendorName);
			assertEquals("Mismatched result for " + connectionUrl, expected, actual); 
		}
	}

}
