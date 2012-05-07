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
package com.springsource.insight.plugin.jdbc.parser;

import java.util.regex.Pattern;

public class SqlParserPattern {

	private final Pattern compiledPattern;
	private final int portIndex;
	private final int databaseNameIndex;
	private final int hostIndex;

	@SuppressWarnings("hiding")
    public SqlParserPattern(final String pattern,
							final int hostIndex,
							final int portIndex,
							final int databaseNameIndex) {
		this.compiledPattern = Pattern.compile(pattern);
		this.portIndex = portIndex;
		this.databaseNameIndex = databaseNameIndex;
		this.hostIndex = hostIndex;
	}

	public Pattern getCompiledPattern() {
		return compiledPattern;
	}

	public int getPortIndex() {
		return portIndex;
	}

	public int getDatabaseNameIndex() {
		return databaseNameIndex;
	}

	public int getHostIndex() {
		return hostIndex;
	}

}
