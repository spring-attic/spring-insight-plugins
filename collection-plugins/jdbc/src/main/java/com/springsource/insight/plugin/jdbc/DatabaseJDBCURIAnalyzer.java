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
package com.springsource.insight.plugin.jdbc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;

/**
 * Locates the JDBC URI of the database from a trace and locates all the external
 * resources represented. The only guarantee is that the resource names
 * returned will be unique.
 * <p/>
 * This class will /try/ to make that unique name a uri, and additionally return
 * useful information about the host and port of the remote resource, but not all
 * jdbc url formats for every database are supported, and not all drivers /can/
 * be supported.
 * <p/>
 * Additional parsers are very welcome.
 */
public abstract class DatabaseJDBCURIAnalyzer implements ExternalResourceAnalyzer {
    protected final OperationType   opType;
	protected DatabaseJDBCURIAnalyzer(OperationType type) {
	    this.opType = type;
	}
	
	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
	    Collection<Frame> dbFrames = trace.getLastFramesOfType(opType);
		if ((dbFrames == null) || dbFrames.isEmpty()) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> dbDescriptors = new ArrayList<ExternalResourceDescriptor>(dbFrames.size());
		for (Frame dbFrame : dbFrames) {
		    Operation op = dbFrame.getOperation();
			String    uri = op.get(OperationFields.CONNECTION_URL, String.class);
			if ((uri == null) || (uri.length() <= 0)) {
				continue;
			}
			dbDescriptors.addAll(extractMeaningfulNames(dbFrame, uri)); 
		}
		
		return dbDescriptors;
	}

	/**
	 * The spec only defines a jdbc url as "jdbc:[vendor].*"
	 * so we try several methods for extracting useful information, some of which
	 * actually work.
	 */
	public List<ExternalResourceDescriptor> extractMeaningfulNames(Frame frame, String connectionString) {
		
		// hope some parser recognizes the string
		List<ExternalResourceDescriptor> parserRecognizedDescriptors = getParserRecognizedDescriptors(frame, connectionString);
		if (parserRecognizedDescriptors != null){
			return parserRecognizedDescriptors;
		}
		
		return getFallbackDescriptor(frame, connectionString);
	}

	private List<ExternalResourceDescriptor> getFallbackDescriptor(
			Frame frame, String connectionString) {
		String workingConnectionString = connectionString.replaceFirst("jdbc:", "");
		int indexOfFirstColon = workingConnectionString.indexOf(':');

		if (indexOfFirstColon <= 0) {
			return Collections.emptyList();
		}
		String jdbcScheme = workingConnectionString.substring(0, indexOfFirstColon);
		String jdbcHash = MD5NameGenerator.getName(connectionString);

		String host = null;
		int port = -1;
		// Try to parse the string as a real uri
		URI uri = extractURI(workingConnectionString);
		if (uri != null) {
			host = uri.getHost();
			port = uri.getPort();			
		}
		
		// for  Non-URI based and special cases the host and port remain default
		ExternalResourceDescriptor hashed = new ExternalResourceDescriptor(frame, jdbcScheme + ":1:" + jdbcHash, "", ExternalResourceType.DATABASE.name(), jdbcScheme, host, port);
		return Arrays.asList(hashed);
	}

	private List<ExternalResourceDescriptor> getParserRecognizedDescriptors(Frame frame,
			String connectionString) {		
		List<JdbcUrlMetaData> urlMetaDataList = DatabaseType.parse(connectionString);

		if (urlMetaDataList != null && urlMetaDataList.size() > 0){
			List<ExternalResourceDescriptor> externalResourceDescriptors = new ArrayList<ExternalResourceDescriptor>();
			int instance = 1;
			for (JdbcUrlMetaData urlMetaData : urlMetaDataList) {
				String databaseName = urlMetaData.getDatabaseName();
				String vendor = urlMetaData.getVendorName();
				String host = urlMetaData.getHost();
				int port = urlMetaData.getPort();
				String jdbcHash = MD5NameGenerator.getName(connectionString);

				ExternalResourceDescriptor descriptor = new ExternalResourceDescriptor(frame, vendor + ":" + instance + ":" + jdbcHash, databaseName, ExternalResourceType.DATABASE.name(), vendor, host, port);
				externalResourceDescriptors.add(descriptor);
				
				//using the same instance index as we're assuming no more than one parser will ever succeed in parsing the same url
				instance++;
			}
			return externalResourceDescriptors;
		}
		
		return null;
	}
	
	/**
	 * Try to pull a uri out of the jdbc url. If no uri is to be found, return null
	 */
	private URI extractURI(String possibleURI) {
		String workingName = possibleURI;
		while (workingName.indexOf(':') > 0) {
			try {
				// extract a host/port if we can
				URI uri = new URI(workingName);
				if (uri.getHost() != null){
					return uri;			
				}
			} catch (URISyntaxException e) {
				// swallow anything that scares us
			}
			workingName = workingName.substring(workingName.indexOf(':') + 1, workingName.length());
		}
		return null;
	}


}
