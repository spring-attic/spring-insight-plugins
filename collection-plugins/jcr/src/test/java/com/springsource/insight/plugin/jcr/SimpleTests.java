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
package com.springsource.insight.plugin.jcr;

import java.io.File;
import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.fs.local.FileUtil;
import org.junit.Assert;

public class SimpleTests {
	private static final String REPOSITORY_CONFIG_PATH="./src/test/resources/repository.xml";
	private static final String REPOSITORY_DIRECTORY_PATH="./target/testdata";
	
	private static Repository repository;
	private static SimpleTests instance;
	
	
	public static SimpleTests getInstance() {
		if (repository==null) {
			init();
		
			instance=new SimpleTests();
		}
		
		return instance;
	}
	
	private SimpleTests() {
		super();
	}
	
	static void init() {
		deleteRepoData();
		
		repository = new TransientRepository(REPOSITORY_CONFIG_PATH, REPOSITORY_DIRECTORY_PATH);
	}
	
	static void close() {
		try {
			JackrabbitRepository jackrabbit = (JackrabbitRepository)repository;
			jackrabbit.shutdown();
		}
		finally {
			deleteRepoData();
		}
	}
	
	/**
	* Clean up the test data
	*/
	private static void deleteRepoData() {
		try {
			FileUtil.delete(new File(REPOSITORY_DIRECTORY_PATH));
		}
		catch(IOException e) {
			//ignore
		}
	}
	
	public void test() throws LoginException, RepositoryException {		
		Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
		Assert.assertNotNull("Cannot login", session);
		
		try { 
			Node root = session.getRootNode();
			Assert.assertNotNull("Cannot retrieve root node", root);

			// Store content 
			Node hello = root.addNode("hello");
			Assert.assertNotNull("Cannot create node", hello);
			hello.setProperty("message", "Hello, World!"); 
			session.save();
			
			// Retrieve content 
			Node node = root.getNode("hello");
			Assert.assertNotNull("Cannot retrieve node by path", node);
			Assert.assertEquals("Invalid node property", node.getProperty("message").getString(), "Hello, World!");
		}
		finally { 
			session.logout();
		}
	}
}
