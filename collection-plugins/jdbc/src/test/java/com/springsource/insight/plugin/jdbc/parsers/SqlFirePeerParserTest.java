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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.parsers.SqlFirePeerParser;


public class SqlFirePeerParserTest {
    
    @Test
    public void testSingleLocator() {
        SqlFirePeerParser parser = new SqlFirePeerParser();
        
        List<JdbcUrlMetaData> res = parser.parse("jdbc:sqlfire:locators=localhost[1234];mcast-port=0", "");
        
        assertNotNull(res);
        assertEquals(1, res.size());
        
        JdbcUrlMetaData data = res.get(0);
        
        assertEquals("localhost", data.getHost());
        assertEquals(1234, data.getPort());
    }
    
    @Test
    public void testMultipleLocators() {
        SqlFirePeerParser parser = new SqlFirePeerParser();
        
        List<JdbcUrlMetaData> res = parser.parse("jdbc:sqlfire:locators=localhost[1234],localhost2[5678];mcast-port=0", "");
        
        assertNotNull(res);
        assertEquals(2, res.size());
        
        JdbcUrlMetaData data1 = res.get(0);
        JdbcUrlMetaData data2 = res.get(1);
        
        assertEquals("localhost", data1.getHost());
        assertEquals("localhost2", data2.getHost());
        
        assertEquals(1234, data1.getPort());
        assertEquals(5678, data2.getPort());
    }
    
    @Test
    public void testInvalidLocators() {
        SqlFirePeerParser parser = new SqlFirePeerParser();
        
        List<JdbcUrlMetaData> res = parser.parse("jdbc:sqlfire:locators=;mcast-port=0", "");
        
        assertNull(res);
    }
    
    @Test
    public void testMulticast() {
        SqlFirePeerParser parser = new SqlFirePeerParser();
        
        List<JdbcUrlMetaData> res = parser.parse("jdbc:sqlfire:;mcast-port=1234", "");
        
        assertNotNull(res);
        assertEquals(1, res.size());
        
        JdbcUrlMetaData data = res.get(0);
        
        assertEquals("", data.getHost());
        assertEquals(1234, data.getPort());
    }
    
    @Test
    public void testEmpty() {
        SqlFirePeerParser parser = new SqlFirePeerParser();
        
        List<JdbcUrlMetaData> res = parser.parse("jdbc:sqlfire:;mcast-port=0", "");
        
        assertNull(res);
    }
}
