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
package com.springsource.insight.plugin.cassandra;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.cassandra.locator.SimpleStrategy;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.springsource.insight.plugin.cassandra.embeded.EmbeddedCassandraService;

public class CassandraUnitTests {
	private static EmbeddedCassandraService cassandra;
	private static CassandraUnitTests instance;
	private static Cassandra.Client lastClient;
	
    public static CassandraUnitTests getInstance() throws Exception {
		if (instance==null) {
			instance=new CassandraUnitTests();
			
	        cassandra = new EmbeddedCassandraService();
	        cassandra.init();
	        Thread t = new Thread(cassandra);
	        t.setDaemon(true);
	        t.start();
		}
        
        return instance;
    }
	
	private Cassandra.Client getClient() throws TTransportException {
		if (lastClient==null) {
			TTransport tr = new TFramedTransport(new TSocket("localhost", 9160));
			lastClient=new Cassandra.Client(new TBinaryProtocol(tr));
			tr.open();
		}
        return lastClient;
	}
	
	public void testConnection() throws TTransportException {
		getClient();
	}
	
	public void testSystemAddKeyspace() throws TTransportException {
		Cassandra.Client client = getClient();
        
        String keyspace = "Keyspace1";
        // create keyspace
        KsDef ksdef = new KsDef(keyspace, SimpleStrategy.class.getName(), new ArrayList<CfDef>());
        //Set replication factor
        if (ksdef.strategy_options == null) {
            ksdef.strategy_options = new LinkedHashMap<String, String>();
        }
        //Set replication factor, the value MUST be an integer
        ksdef.strategy_options.put("replication_factor", "1");
        
        try {
	    	client.system_add_keyspace(ksdef);
		}
		catch(Exception e) {
			System.err.println("testSystemAddKeyspace: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	public void testSetKeyspace() throws Exception {
		Cassandra.Client client = getClient();
        
        String keyspace = "Keyspace1";
        client.set_keyspace(keyspace);
	}
	
	public void testSystemAddColumnFamily() throws TTransportException  {
		Cassandra.Client client = getClient();
		
        String keyspace = "Keyspace1";
        //record id
        String columnFamily = "Standard1";
        // create columnfamily
        try {
        	client.system_add_column_family(new CfDef(keyspace, columnFamily));
		} catch(Exception e) {
			System.err.println("testSystemAddColumnFamily: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	public void testInsert() throws Exception {
    	// prepare keyspace
    	testSystemAddKeyspace();

    	// set keyspace
    	testSetKeyspace();
    	// prepare keyfamily
    	testSystemAddColumnFamily();
    	
    	Cassandra.Client client = getClient();   

        //record id
        String key_user_id = "1";
        String columnFamily = "Standard1";
        // insert data
        Column nameColumn = new Column(ByteBuffer.wrap("name".getBytes()));
        nameColumn.setValue("John Dow".getBytes());
        nameColumn.setTimestamp(System.currentTimeMillis());

        ColumnParent columnParent = new ColumnParent(columnFamily);
        try {
        	client.insert(ByteBuffer.wrap(key_user_id.getBytes()), columnParent,nameColumn,ConsistencyLevel.ALL);
		}
		catch(Exception e) {
			System.err.println("testInsert: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
    public void testGetSlice() throws Exception {
    	testInsert();
    	
    	Cassandra.Client client = getClient();
    	
        //record id
        String key_user_id = "1";
        String columnFamily = "Standard1";
        ColumnParent columnParent = new ColumnParent(columnFamily);

        //Gets column by key
        SlicePredicate predicate = new SlicePredicate();
        predicate.setSlice_range(new SliceRange(ByteBuffer.wrap(new byte[0]), ByteBuffer.wrap(new byte[0]), false, 100));
        List<ColumnOrSuperColumn> columnsByKey = client.get_slice(ByteBuffer.wrap(key_user_id.getBytes()), columnParent, predicate, ConsistencyLevel.ALL);
        System.out.println(columnsByKey);

            /*//Get all keys
            KeyRange keyRange = new KeyRange(100);
            keyRange.setStart_key(new byte[0]);
            keyRange.setEnd_key(new byte[0]);
            List<KeySlice> keySlices = client.get_range_slices(columnParent, predicate, keyRange, ConsistencyLevel.ONE);
            System.out.println(keySlices.size());
            System.out.println(keySlices);
            for (KeySlice ks : keySlices) {
                    System.out.println(new String(ks.getKey()));
            }*/
       //client.getInputProtocol().getTransport().close();
    }
}