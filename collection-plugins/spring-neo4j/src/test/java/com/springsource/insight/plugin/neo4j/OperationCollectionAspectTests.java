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
package com.springsource.insight.plugin.neo4j;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.Traversal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/test-context.xml"})
@Transactional
public class OperationCollectionAspectTests {
	@Autowired Neo4jTemplate template;
	
	
	public void test_Init() {
		template.setInfrastructure(template.getInfrastructure());
	}
	
	@Test
	public void test_Find() {
	    Movie movie = template.save(new Movie(1, "Forrest Gump", 1994));
		template.findOne(movie.getNodeId(), Movie.class);
	}
	
	@Test
	public void test_Lookup1() {
		Node thomas = template.createNode();
		template.index("sampleIndex", thomas, "sampleField", "sampleValue");
		
		template.lookup("sampleIndex","sampleField:*");
	}
	
	@Test
	public void test_Lookup2() {
		Node thomas = template.createNode();
		template.index("sampleIndex", thomas, "sampleField", "sampleValue");
		
		template.lookup("sampleIndex","sampleField","sampleValue");
	}
	
	@Test
	public void test_Lookup3() {
		try {
			template.lookup(String.class,"sampleProperty","sampleValue");
		}
		catch(Exception e) {
			
		}
	}
	
	@Test
	public void test_Query1() {
		template.query("start n=node(0) return n", map("paramKey", "paramValue"));
	}
	
	@Test
	public void test_Query2() {
		try {
			template.execute("sampleStatement", map("paramKey", "paramValue"));
		}
		catch(Exception e) {
			
		}
	}
	
	@Test
	public void test_Traverse1() {
		try {
			template.traverse(template.createNode(), Traversal.description());
		}
		catch(Exception e) {
			
		}
	}
	
	@Test
	public void test_Traverse2() {
		try {
			template.traverse("sampleEntity", String.class, Traversal.description());
		}
		catch(Exception e) {
			
		}
	}
	
	private Map<String,Object> map(String key, Object value) {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put(key, value);
		return map;
	}
}
