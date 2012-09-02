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

package com.springsource.insight.plugin.jdbc.parser;

import java.util.List;

/**
 * Parses JDBC connection URL(s)
 */
public interface JdbcUrlParser {
	static final String	JDBC_PREFIX="jdbc";
    static final String DEFAULT_HOST = "localhost";
    static final String DEFAULT_DB_NAME = "", UNKNOWN_DATABASE="<unknown>";
    static final int DEFAULT_PORT = -1;

    /**
     * @return The immediate vendor name after the <code>jdbc</code> prefix
     */
    String getVendorName ();

    List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName);
}
